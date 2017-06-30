import com.sun.corba.se.spi.orbutil.fsm.Guard;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;

import java.util.ArrayList;

/**
 * Created by micha on 6/27/2017.
 */

//give this class a Sequence, a list of modifications and a spectrum and it will check which ions are part of the EC complementary ion cluster
    //also check if fragmentation of EC occurred
    //return an ArrayList of compClusterIonMatches
    //ArrayList of Mods can be empty, only filled if other modification than EC-modification is present
public class ComplementaryClusterChecker {
    public static ArrayList<CompClusterIonMatch> compClusterCheckerEC (ArrayList<AminoAcid> acids,
                                                                       String SequenceIn,
                                                                       ArrayList<Modification> modsIn,
                                                                       String spectrumID, MzXMLFile completeFileIn,
                                                                       double accuracy) throws JMzReaderException {
        ArrayList<IonMatch> successfulMatches = new ArrayList<>();
        //generate spectrum to look at:
        MySpectrum spectrumToCheck = MzXMLReadIn.mzXMLToMySpectrum(completeFileIn, spectrumID);
        String spectrumHeader = spectrumToCheck.getScanHeader();


        //create unmodified peptide
        Peptide idPeptide = new Peptide(SequenceIn, acids);
        //determine how many EC modifications are present
        int lysCount = 0;
        ArrayList<AminoAcid> sequence = new ArrayList<>();
        sequence.addAll(idPeptide.getAminoAcidsList());
        for (AminoAcid acid : sequence){
            if (acid.get1Let() == 'K')
                lysCount++;
        }
        int countEC = lysCount+1;
        //create different mods for the different possibilities (cleaved/noncleaved)
        ArrayList<ArrayList<Modification>> allMods = new ArrayList<>();
        allMods = modCreator(idPeptide, countEC, modsIn);

        //generate List of different modified peptides to check for every possibility (EC cleaved/EC not cleaved)
        ArrayList<Peptide> modifiedPeptides = new ArrayList<>();
        for (ArrayList<Modification> modList : allMods){
            Peptide modifiedPeptide = new Peptide(SequenceIn, acids);
            modifiedPeptides.add(modifiedPeptide.peptideModifier(modList));
        }
        //invoke PeakCompare function and store result matches in List of IonMatches
        for (Peptide modPeptide : modifiedPeptides){
            System.out.println("Peptide: "+modPeptide.getSequence());
            successfulMatches.addAll(PeakCompare.peakCompare(spectrumToCheck, modPeptide, accuracy));
            System.out.println();
        }

        ArrayList<CompClusterIonMatch> relevantMatches = new ArrayList<>();
        relevantMatches = relevantMatchesPicker(successfulMatches, spectrumHeader);

        return relevantMatches;
    }


    //this function should create all the different possibilities for EC modifications
    //cleaved and uncleaved, EC179 and EC180 complementary ion-clusters
    public static ArrayList<ArrayList<Modification>> modCreator(Peptide pepToModify, int countECIn, ArrayList<Modification> modsIn) {
        //create positions of modified lysines
        //lysPos is int[] with all the positions of EC-Modifications(N-Term + Lys) in the sequence
        int lysCount = countECIn - 1;
        int[] lysPos = new int[lysCount];
        int lysPosPointer = 0;
        ArrayList<AminoAcid> sequence = new ArrayList<>();
        sequence.addAll(pepToModify.getAminoAcidsList());
        for (int a = 0; a < sequence.size(); a++) {
            if (sequence.get(a).get1Let() == 'K') {
                lysPos[lysPosPointer] = a + 1;
                lysPosPointer++;
            }
        }
        //create new list of modification lists
        ArrayList<ArrayList<Modification>> completeList = new ArrayList<>();
        //create the first 3 lists: 3 different possible modifications, all on NTerm
        for (int e = 0; e < 3; e++) {
            ArrayList<Modification> current = new ArrayList<>();
            current.addAll(modsIn);
            current.add(modChooser(e, 1));
            completeList.add(current);
        }
        //now loop through all the lysines and overwrite the existing list
        //do so by using the listMultiplier method
        //by this, create all possibilities...also those which aren't occuring
        for (int f = 0; f < lysCount; f++) {
            completeList = listMultiplier(completeList, lysPos[f]);
        }

        //completeList is now longer than it needs to be
        //one can remove all the cases where different cleaved modifications occur. e.g. cleaved-EC179, cleaved-EC180 is unnecessary
        //create new reduced list
        ArrayList<ArrayList<Modification>> reducedList = new ArrayList<>();

        //loop through all the modlists in the completeList(of lists)
        for (ArrayList<Modification> modList : completeList) {
            //check now what their modification names are
            boolean differentCleavedTags = false;
            String firstCleavedMod = "";
            //this loop searches for the first occurrence of a cleaved modification:EC179_cleaved or EC180_cleaved
            for (Modification mod : modList) {
                if (mod.getModificationName().contains("179") || mod.getModificationName().contains("180")) {
                    firstCleavedMod = mod.getModificationName();
                    break;
                }
            }
            //this loop now checks again: are there cleaved mods(containing 179 or 180)
            for (Modification mod : modList) {
                //is it a cleaved mod?
                if (mod.getModificationName().contains("179") || mod.getModificationName().contains("180")) {
                    //has it another name?
                    if (mod.getModificationName() != firstCleavedMod) {
                        //there are different cleaved mods! list should be sorted out
                        differentCleavedTags = true;
                        break;
                    }
                }
            }
            //only add the usefulf lists
            if (!differentCleavedTags) {
                reducedList.add(modList);
            }
        }

        return reducedList;
    }


    private static ArrayList<ArrayList<Modification>> listMultiplier(ArrayList<ArrayList<Modification>> listIn, int pos){
        ArrayList<ArrayList<Modification>> listOut = new ArrayList<>();
        for (ArrayList<Modification> modLists : listIn){
            for (int i = 0; i<3;i++){
                ArrayList<Modification> currentList = new ArrayList<>();
                currentList.addAll(modLists);
                currentList.add(modChooser(i, pos));
                listOut.add(currentList);
            }
        }
        return listOut;
    }



    private static Modification modChooser(int modChooser, int pos){
        Modification mod = null;
        switch(modChooser){
            case 0:
                mod = Modification.uncleavedECDuplex(pos);
                break;
            case 1:
                mod = Modification.cleavedEC179(pos);
                break;
            case 2:
                mod = Modification.cleavedEC180(pos);
                break;
        }
        return mod;
    }

    private static ArrayList<CompClusterIonMatch> relevantMatchesPicker(ArrayList<IonMatch> completeListIn, String scanHeaderIn){
        ArrayList<CompClusterIonMatch> relevantList = new ArrayList<>();
        //loop through every match in the complete IonMatch list
        for (IonMatch match : completeListIn){
            //check if the fragment Ion contains the label
            if (match.getFragmentIon().getLabelStatus())
            {
                //in case the label is detected, determine if it is uncleaved, cleaved or mixed and get the modification name
                int labelQuantity = match.getFragmentIon().getLabelQuantity();
                ArrayList<AminoAcid> labeledAAs = new ArrayList<>();
                labeledAAs = match.getFragmentIon().getLabelAAs();
                String labelName = "";
                boolean isCleavedOnly = false;
                boolean isMixed=false;
                for (int a = 0; a < labelQuantity; a++){
                    //determine label name
                    //if there are multiple Labels present, then insert ; in between names
                    if (!labelName.isEmpty())
                        labelName += ";";
                    labelName +=  labeledAAs.get(a).getModification().getModificationName();
                    //determine if cleavage status is all uncleaved (isCleavedOnly = false, isMixed = false), all cleaved (isCleavedOnly= true, isMixed = false)
                    //in case of mixing: isCleavedOnly=false, isMixed=true
                    isCleavedOnly = labeledAAs.get(a).getModification().getCleavedStatus(); //cleaved: true; intact=false
                    //if only one variable is present, then exit here
                    if (a==0)
                        break;
                    //if isCleavedOnly was determined before
                    boolean nextLabel;
                    nextLabel = !(isCleavedOnly^labeledAAs.get(a).getModification().getCleavedStatus());//next label gets inverted XOR statement
                    if (nextLabel != isCleavedOnly)
                        isMixed=true; //one difference is enough to set isMixed to true
                    isCleavedOnly = nextLabel;
                }
                CompClusterIonMatch importantMatch = new CompClusterIonMatch(match.getFragmentIon(), match.getPeak(),
                                                                                match.getPpmDeviation(),
                                                                                labelName,isCleavedOnly, isMixed, scanHeaderIn);
                relevantList.add(importantMatch);



            }
        }

        return relevantList;
    }



}
