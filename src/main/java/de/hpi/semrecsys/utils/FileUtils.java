package de.hpi.semrecsys.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class FileUtils {
	public static int aSizeOfByteArray = 1024;

	public static void writeTextToFile(String aText, String aFileName, boolean needAppend) {
		File aFile = new File(aFileName);
		if (!aFile.exists())
			try {
				recreateFile(aFileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		try {
			BufferedWriter aBufferedWriter = openUtf8Writer(aFile.getAbsolutePath(), needAppend);
			aBufferedWriter.write(aText);
			aBufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String readTextFromFile(String aFileName) {
		return readTextFromFile(aFileName, -1);
	}

	/**
	 * 
	 * @param aFileName
	 * @param numberOfLines
	 *            to read. use -1 if without bordering.
	 * @return
	 * @throws IOException
	 */
	public static String readTextFromFile(String aFileName, int numberOfLines) {
		File file = new File(aFileName);
		return readTextFromFile(file, numberOfLines);
	}

	public static String readTextFromFile(File file) {
		return readTextFromFile(file, -1);
	}

	public static String readTextFromFile(File file, int numberOfLines) {
		FileReader aFileReader;
		BufferedReader aBufferedReader = null;
		String result = "";
		try {
			aFileReader = new FileReader(file);
			aBufferedReader = new BufferedReader(aFileReader);

			String aTextLine = null;
			int lineNumber = 0;
			while ((aTextLine = aBufferedReader.readLine()) != null) {
				if (lineNumber > 0) {
					result += "\n";
				}

				result += aTextLine;
				if (numberOfLines > 0 && lineNumber > numberOfLines) {
					break;
				}
				lineNumber++;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (aBufferedReader != null) {
			try {
				aBufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static List<String> readTextFromFileToLines(String fileName) {
		return readTextFromFileToLines(new File(fileName), -1);
	}

	public static List<String> readTextFromFileToLines(File file) {
		return readTextFromFileToLines(file, -1);
	}

	public static List<String> readTextFromFileToLines(File file, int numberOfLines) {
		List<String> fileLines = new ArrayList<String>();

		BufferedReader aBufferedReader = null;

		try {

			aBufferedReader = openUtf8Reader(file.getAbsolutePath());

			String aTextLine = null;
			int lineNumber = 0;
			while ((aTextLine = aBufferedReader.readLine()) != null) {

				fileLines.add(aTextLine);
				if (numberOfLines > 0 && lineNumber > numberOfLines) {
					break;
				}
				lineNumber++;

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (aBufferedReader != null) {
			try {
				aBufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return fileLines;
	}

	public static BufferedReader openReader(String fileName) throws Exception {
		File aFile = new File(fileName);

		FileReader aFileReader = new FileReader(aFile);
		BufferedReader aBufferedReader = new BufferedReader(aFileReader);
		return aBufferedReader;
	}

	private static void removeFileOrDirectory(File aFile) throws IOException {
		if (aFile.exists()) {
			if (aFile.isDirectory()) {
				for (File aChildFile : aFile.listFiles()) {
					removeFileOrDirectory(aChildFile);
				}
			}
			try {

				if (!aFile.delete()) {
					throw new IOException("The file " + aFile.getPath() + " cannot be deleted");
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		} else {
			throw new IOException("The file " + aFile.getPath() + " is not exists");
		}
	}

	public static void removeFileOrDirectory(String aFileName) throws IOException {
		removeFileOrDirectory(new File(aFileName));
	}

	public static void recreateDirectories(String aFileName) throws IOException {
		File aFile = new File(aFileName);
		if (aFile.exists()) {
			removeFileOrDirectory(aFile);
		}
		if (!aFile.mkdirs())
			throw new IOException("a directory " + aFileName + " couldn't be created");
	}

	public static void recreateFile(String aFileName) {
		File aFile = new File(aFileName);
		String aParentPath = aFile.getParent();
		try {
			if (aFile.exists() && !aFile.delete())
				throw new IOException("Cannot delete file:" + aFile.getAbsolutePath());
			if (aParentPath != null && !new File(aParentPath).exists()) {
				recreateDirectories(aParentPath);
			}
			aFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns size of a file with aDataFilePath in bytes
	 * 
	 * @param aDataFilePath
	 *            a path to file
	 * @return
	 */
	public static int getFileSize(String aDataFilePath) {
		return (int) new File(aDataFilePath).length();
	}

	public static BufferedWriter openUtf8Writer(String sideFilePath, boolean append) throws FileNotFoundException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sideFilePath, append),
				Charset.forName("UTF-8")));
	}

	public static BufferedReader openUtf8Reader(String inputFilePath) throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(inputFilePath), Charset.forName("UTF-8")));
	}

	public static boolean confirmAndDelete(String path) throws IOException {
		return confirmAndDelete(new File(path));
	}

	public static boolean confirmAndDelete(File file) throws IOException {
		if (!file.exists()) {
			return true;
		} else {
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Delete " + file.getAbsolutePath() + "? (y/n) ");
			String answer;
			for (answer = "?"; !answer.equals("y") && !answer.equals("n");) {
				answer = console.readLine();
			}
			if (answer.equals("y")) {
				removeFileOrDirectory(file);
				return true;
			} else {
				return false;
			}
		}
	}

	public static File getCurrentPath() {
		String encodedPath = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		File pathFile = null;
		try {
			pathFile = new File(URLDecoder.decode(encodedPath, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return pathFile;
	}

	public static InputStream getResourceAsStream(String filename) {
		InputStream in = FileUtils.class.getClassLoader().getResourceAsStream(filename);
		return in;
	}

	public static Properties readProperties(String propertiesPath) {
		Properties properties = new Properties();
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propertiesPath);
			properties.load(inputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}

}