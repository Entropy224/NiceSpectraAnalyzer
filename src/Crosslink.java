import java.util.ArrayList;

public class Crosslink {


    private Peptide peptideAlpha;   // alpha is the longer peptide
    private Peptide peptideBeta;    //beta is the smaller peptide
    private String xlUsed;
    private SumFormula xlSumFormula;
    private Ion theoreticalXLIon;
    private double isolatedMassToCharge;
    private int scanNumber;
    private String fragmentationMethod;
    private boolean monoisotopicSelected;
    private int monoisotopicPeakOffset;

    public Crosslink (String peptide1In, String peptide2In, String xlIn, int chargeStateIn,
                      int scanNumberIn, String fragmentationMethodIn, double isolatedMassToChargeIn, ArrayList<AminoAcid> aaIn){

        //only cliXlink is supported atm
        if (!xlIn.equals("cliXlink"))
            throw new IllegalArgumentException("Unknown crosslinker: "+ xlIn);
        //easy to set variables
        this.xlUsed = xlIn;
        this.scanNumber = scanNumberIn;
        this.fragmentationMethod = fragmentationMethodIn;
        this.isolatedMassToCharge = isolatedMassToChargeIn;

        //utilize the xlPeptideModification function to prepare the peptides with their modifications from merox
        if (peptide1In.length() > peptide2In.length()){
            this.peptideAlpha = xlPeptideModification(peptide1In,aaIn);
            this.peptideBeta = xlPeptideModification(peptide2In, aaIn);
        }
        else{
            this.peptideAlpha = xlPeptideModification(peptide2In,aaIn);
            this.peptideBeta = xlPeptideModification(peptide1In, aaIn);
        }

        //create the combined theoretical ion
        //combine sum formulas from the peptides and the sum formula of the crosslinker
        this.xlSumFormula = SumFormula.sumFormulaJoiner(peptideAlpha.getSumFormula(),
                SumFormula.sumFormulaJoiner(peptideBeta.getSumFormula(), SumFormula.getCliXlinkFormula()));
        //add protons according to charge state? necessary?
        //TODO: check if protons have to be added for all the classes to work
        for (int i = 1; i < chargeStateIn+1; i++)
        {
            this.xlSumFormula = SumFormula.sumFormulaJoiner(this.xlSumFormula, SumFormula.getProtonFormula());
        }

        this.theoreticalXLIon = new Ion(this.xlSumFormula, chargeStateIn);

        //check if experimental isolated m/z and theoretical mass match or a higher isotope peak was isolated
        double theoMassToCharge = theoreticalXLIon.getMToZ();
        //set ppm Tolerance to something
        double ppmDev = 10;
        //set the default value to something unobtainable high
        this.monoisotopicPeakOffset = 50;
        if (DeviationCalc.ppmMatch(theoMassToCharge, isolatedMassToChargeIn, ppmDev)){
            this.monoisotopicSelected = true;
            this.monoisotopicPeakOffset = 0;
        }
        else{
            this.monoisotopicSelected = false;
            for (int n = 1; n <10; n++){
                double shiftedMass = theoMassToCharge + (AtomicMasses.getNEUTRON() * n / chargeStateIn);
                if (DeviationCalc.ppmMatch(shiftedMass, isolatedMassToChargeIn, ppmDev)) {
                    this.monoisotopicPeakOffset = n;
                    break;
                }
            }

        }






        this.theoreticalXLIon = new Ion();











    }
    //this functions deals with adding the static modifications and the optional Methionine-Oxidation
    //oxidation is specified by small letter 'm'
    private static Peptide xlPeptideModification(String sequenceIn, ArrayList<AminoAcid> aaListIn){
        Peptide out;
        //this list will handle all the modifications
        ArrayList<Modification> modList = new ArrayList<>();

        //check if cysteines are present - they are output by MeroX as B because of the carbamidomethylation
        //also check if oxidized methionines exist - labeled by merox with small letter m
        if (sequenceIn.contains("B") || sequenceIn.contains("m")){
            //handle static carbamidomethylation
            if (sequenceIn.contains("B"))
                modList.add(Modification.carbamidomethylation());
            //to change sequence string, copy string to string builder and do the required changes
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sequenceIn.length()-1; i++){
                char c = sequenceIn.charAt(i);
                switch (c){
                    case 'B':
                        sb.append("C");
                        //the carbamidomethylation modification should only be added to the list once, so that will be handeled in the beginning
                        break;
                    case 'm':
                        sb.append("M"); //doesn't really matter, since Peptide converts to Upper Case anyway
                        modList.add(Modification.oxidation(i));
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            //stringbuilder contains modified sequence now
            String modifiedSequence = sb.toString();
            out = new Peptide(modifiedSequence, aaListIn);
            out = out.peptideModifier(modList);
            return out;
        }
        //otherwise, the peptide can be parsed as-is
        else
            out = new Peptide(sequenceIn, aaListIn);

        return out;
    }
}
