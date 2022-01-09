
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.regex.Pattern;

public class formatConverter {
	public static final int MAX_LINES_PER_SLIDE = 5;//specify max lines per slide
	
	public static int num;
	public static String line;
	public static boolean chorusValid = false;

	public static String songNameGroupId = "Intro"; // presenter pro identifier names
	public static String chorusGroupId = "Chorus";
	public static String verseGroupId = "Verse";

	public static StringWriter songChorus;
	public static PrintStream songOutFile;

	public static int largestVerseCount = 0;

	public static void main(String[] args) throws IOException {

		File inFilesDirectory = new File("InputFiles");// place input text-files into this directory
		File outFilesDirectory = new File("outputFiles");

		if (!inFilesDirectory.isDirectory()) {
			inFilesDirectory.mkdir();
			System.out.println("Error InputFiles Directory is empty");
			System.exit(0);

		}

		if (!outFilesDirectory.isDirectory()) {
			outFilesDirectory.mkdir();

		}

		File[] infileArr = inFilesDirectory.listFiles();

		if (infileArr.length == 0) {
			System.out.println("InputFiles Directory is empty");
			System.exit(0);

		}

		for (File inFile : infileArr) {//looping through all text files

			String songInFileName = inFile.getName();

			num = Integer.parseInt(songInFileName.replaceAll("[^0-9]", ""));// extracts song number from text filename

			Scanner scnr = new Scanner(inFile);

			int lineCount = 1;

			try {

				while (scnr.hasNextLine()) {

					if (lineCount == 1) {// song title

						line = skipBlankLines(scnr, line);// gets first non-blank line - (song title)

						String songOutFileName = String.format(line + ".txt");

						songOutFileName = songOutFileName.replaceAll("[^0-9-A-Za-z,.\s]", "");// prevents illegal file
																								// name characters

						System.out.println(line);

						String outFilePath = "OutputFiles\\" + songOutFileName;

						songOutFile = new PrintStream(new File(outFilePath));
						songOutFile.println(songNameGroupId);
						songOutFile.println(line);

					} else { // song first verse

						line = skipBlankLines(scnr, line);
						addVerse(songOutFile, scnr);

					}
					lineCount++;
				}

				if (chorusValid)
					printChorus(songOutFile);

			} catch (Exception e) {
				System.out.println("\nError someting went wrong with number " + num + "\n");

			}

			chorusValid = false;
			songOutFile.close();
			scnr.close();

		}

	}

	public static void addVerse(PrintStream songOutFile, Scanner scnr) {
		int verseLineCount = 0;
		int verseNum = 0;

		if (line.equals("Refrain")) {
			chorusValid = true;
			storeChorus(songOutFile, scnr, line);

		} else if (line.equals("Last Refrain")) {

			storeChorus(songOutFile, scnr, line);// chorus is updated

		} else {// if verse(s) or single untitled chorus
			String regex = "[0-9]+";
			String inVerseNum = line.replaceAll("[\s]", "");// gets rid of all spaces in potential verse number line and
															// extracts potential verse number

			if (Pattern.matches(regex, inVerseNum)) { // gets verse number if verse number is available
				verseNum = Integer.parseInt(inVerseNum);

				line = skipBlankLines(scnr, line);// If current line has only verse number proceeding lines are verse
													// text

			} else {
				verseNum = 1; // untitled single verse/chorus is given a verse group identifier

			}

			if (chorusValid)
				printChorus(songOutFile);// chorus is printed if chorus is present in song.

			while (scnr.hasNextLine() && !line.isBlank()) {
				verseLinesLimiter(songOutFile, verseNum, verseLineCount);
				songOutFile.println(line);

				line = scnr.nextLine();
				verseLineCount++;

				if (!scnr.hasNextLine()) {// accounts for last line in the event of EOF and formats according the
											// display specification
					verseLinesLimiter(songOutFile, verseNum, verseLineCount);
					songOutFile.println(line);

				}

			}
		}
	}
	
	public static void storeChorus(PrintStream songOutFile, Scanner scnr, String line) {
		int verseLineCount = 0;

		songChorus = new StringWriter();
		line = skipBlankLines(scnr, line);

		while (scnr.hasNextLine() && !line.isBlank()) {

			chorusLinesLimiter(verseLineCount);
			songChorus.append(line + "\n");

			line = scnr.nextLine();
			verseLineCount++;

			if (!scnr.hasNextLine()) {// accounts for last line in the event of EOF and formats according the display
										// specification

				chorusLinesLimiter(verseLineCount);
				songChorus.append(line);
			}

		}

	}

	public static String skipBlankLines(Scanner scnr, String line) {

		line = scnr.nextLine();

		while (scnr.hasNextLine() && line.isBlank()) {
			line = scnr.nextLine();

		}
		return line;
	}

	public static void verseLinesLimiter(PrintStream songOutFile, int verseNum, int verseLineCount) {// limit according
																										// to max
																										// display lines
																										// per verse

		if (verseLineCount % (MAX_LINES_PER_SLIDE) == 0) {
			songOutFile.println("\n" + verseGroupId + " " + verseNum);

		}

	}

	public static void chorusLinesLimiter(int verseLineCount) {// limit according to max display lines per verse

		if (verseLineCount % (MAX_LINES_PER_SLIDE) == 0) {

			songChorus.append("\n" + chorusGroupId + "\n");
		}

	}

	public static void printChorus(PrintStream songOutFile) {
		songOutFile.print(songChorus);

	}


}
