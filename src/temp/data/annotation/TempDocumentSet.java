package temp.data.annotation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ark.data.annotation.DocumentInMemory;

public class TempDocumentSet {
	private List<TempDocument> documents;
	
	public TempDocumentSet() {
		this.documents = new ArrayList<TempDocument>();
	}
	
	public List<TempDocument> getDocuments() {
		return this.documents;
	}
	
	public boolean addDocument(TempDocument document) {
		this.documents.add(document);
		return true;
	}
	
	public boolean saveToJSONDirectory(String directoryPath) {
		for (TempDocument document : this.documents) {
			System.out.print("Outputting " + document.getName() + "... ");
			document.saveToJSONFile(new File(directoryPath, document.getName() + ".json").getAbsolutePath());
			System.out.println("Done.");
		}
		
		return true;
	}
	
	public boolean saveToXMLDirectory(String directoryPath) {
		for (TempDocument document : this.documents) {
			document.saveToJSONFile(new File(directoryPath, document.getName() + ".xml").getAbsolutePath());
		}
		
		return true;
	}
	
	public static TempDocumentSet loadFromJSONDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		TempDocumentSet documentSet = new TempDocumentSet();
		try {
			if (!directory.exists() || !directory.isDirectory())
				throw new IllegalArgumentException("Invalid directory: " + directory.getAbsolutePath());
			
			List<File> files = new ArrayList<File>();
			files.addAll(Arrays.asList(directory.listFiles()));
			int numTopLevelFiles = files.size();
			for (int i = 0; i < numTopLevelFiles; i++)
				if (files.get(i).isDirectory())
					files.addAll(Arrays.asList(files.get(i).listFiles()));
			
			List<File> tempFiles = new ArrayList<File>();
			for (File file : files) {
				if (!file.isDirectory() && file.getName().endsWith(".json")) {
					tempFiles.add(file);
				}
			}
			
			Collections.sort(tempFiles, new Comparator<File>() { // Ensure determinism
			    public int compare(File o1, File o2) {
			        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			    }
			});
			
			for (File file : tempFiles) {
				System.out.println("Loading document " + file.getName());
				documentSet.addDocument(new TempDocument(file.getAbsolutePath(), DocumentInMemory.StorageType.JSON));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
		
		return documentSet;
	}
	
	public static TempDocumentSet loadFromXMLDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		TempDocumentSet documentSet = new TempDocumentSet();
		try {
			if (!directory.exists() || !directory.isDirectory())
				throw new IllegalArgumentException("Invalid directory: " + directory.getAbsolutePath());

			List<File> files = new ArrayList<File>();
			files.addAll(Arrays.asList(directory.listFiles()));
			int numTopLevelFiles = files.size();
			for (int i = 0; i < numTopLevelFiles; i++)
				if (files.get(i).isDirectory())
					files.addAll(Arrays.asList(files.get(i).listFiles()));
			
			List<File> tempFiles = new ArrayList<File>();
			for (File file : files) {
				if (!file.isDirectory() && !file.getName().endsWith(".xml"))
					continue;
				tempFiles.add(file);
			}
			
			Collections.sort(tempFiles, new Comparator<File>() { // Ensure determinism
			    public int compare(File o1, File o2) {
			        return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
			    }
			});
			
			for (File file : tempFiles)
				documentSet.addDocument(new TempDocument(file.getAbsolutePath(), DocumentInMemory.StorageType.XML));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
		
		return documentSet;
	}
}