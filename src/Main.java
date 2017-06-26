import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.CvParam;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.ParamGroup;
import uk.ac.ebi.pride.tools.jmzreader.model.impl.UserParam;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLSpectrum;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException {
        DecimalFormat fiveDec = new DecimalFormat("0.00000");

        /*String filePath = "C:\\Anwendungen\\IntelliJProjects\\NiceSpectraAnalyzer\\NiceSpectraAnalyzer\\Aminoacids_list.csv";
        File aminoAcids = new File(filePath);
        ArrayList<AminoAcid> aminoAcidsList = new ArrayList<>();
        aminoAcidsList = CSVReader.aminoAcidParse(aminoAcids);

        File spectrum = new File("C:\\Users\\micha\\Desktop\\5451.csv");
        MySpectrum testSpectrum = CSVReader.spectrumParse(spectrum);

        Peptide pepA = new Peptide("AAALAAADAR", aminoAcidsList);
        ArrayList<Modification> mods = new ArrayList<>();
        mods.add(Modification.uncleavedECDuplexNTerm());
        Peptide pepAMod = pepA.peptideModifier(mods);
        //pepAMod.createAddFragmentIonChargestate(2);
        System.out.println("");
        System.out.println("");
        ArrayList<FragmentIon> fragments = pepAMod.getbIons();
        fragments.addAll(pepAMod.getyIons());
        for (FragmentIon fragment : fragments){
            //FragmentIon.fragmentIonFormulaPrinter(fragment);
            FragmentIon.fragmentIonPrinter(fragment);
        }
       ArrayList<IonMatch> matchedIons = PeakCompare.peakCompare(testSpectrum, pepAMod, 5);
*/

        String filePath = "C:\\Users\\Michael Stadlmeier\\Desktop\\Programmierzeugs\\complete\\20170519_stamch_ECDuplex_NEBBSA_newbatches_1to1_R1.mzxml";
        File spectrumFile = new File(filePath);
        MzXMLFile mzXMLFile = new MzXMLFile(spectrumFile);
        System.out.println("Number of spectra: "+mzXMLFile.getSpectraCount());

        for (int i = 1; i<mzXMLFile.getSpectraCount()+1;i++) {
            MySpectrum convertedSpectrum = MzXMLReadIn.mzXMLToMySpectrum(mzXMLFile, Integer.toString(i));
            System.out.println("Generating spectra: "+i);
        }









    }
}
