import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.IndexElement;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Peaks;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Michael Stadlmeier on 6/13/2017.
 * for more information, please contact me!
 */
public class Main {

    public static void main(String[] args) throws MzXMLParsingException, JMzReaderException, IOException, InterruptedException {
        DecimalFormat fourDec = new DecimalFormat("0.0000");
        DecimalFormat twoDec = new DecimalFormat("0.00");
        DecimalFormat xDec = new DecimalFormat("0.000000000");


        String peptidePath = "C:\\Programmingfolder\\Targeted\\TGR_10582_Tar3_BSAmix_C10_pro0Search.csv";
        String runPath = "C:\\Programmingfolder\\Benenodin\\Lumos_data\\TGR_10744_Bene1_D1_CIDScreen_low_1uL.mzXML";
        String massListPath = "C:\\Programmingfolder\\Benenodin\\mass list\\Benenodin1_massList.csv";
        File runFile = new File(runPath);
        //MzXMLFile runMZXML = new MzXMLFile(runFile);
        //Visualization.spectrumPlotter(runMZXML, 10);
        //DnD.willNimaDie(100000000);
        //RandomTools.ms1PrecursorInfoAdvanced(peptidePath, runPath);

        RandomTools.fixTMTproCOutput("C:\\Programmingfolder\\Various\\TGR_10805_SR2_Tar3_EColi_TMTproC_60min_1ug_unfiltered.csv");
































































    }





    }

