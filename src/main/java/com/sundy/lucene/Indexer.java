package com.sundy.lucene;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {

	private IndexWriter indexWriter;
	
	public Indexer(String indexDir) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		indexWriter = new IndexWriter(dir, new StandardAnalyzer(Version.LUCENE_30),true,IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	public int index(String dataDir,FileFilter fileFilter) throws CorruptIndexException, IOException{
		File[] files = new File(dataDir).listFiles();
		for(File file : files){
			if(!file.isDirectory()&&
					!file.isHidden()&&
					file.exists()&&
					file.canRead()&&
					(fileFilter==null||fileFilter.accept(file))){
				indexFile(file);
			}
		}
		return indexWriter.numDocs();
	}
	
	private void indexFile(File file) throws CorruptIndexException, IOException {
		System.out.println("Indexing "+file.getCanonicalPath());
		Document doc = getDocument(file);
		indexWriter.addDocument(doc);
	}

	private Document getDocument(File file) throws IOException {
		Document doc = new Document();
		doc.add(new Field("contents",new FileReader(file)));
		doc.add(new Field("filename", file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath",file.getCanonicalPath(),Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

	private void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}
	
	private static class TextFilesFilter implements FileFilter{

		public boolean accept(File path) {
			return path.getName().endsWith(".txt");
		}
		
	}
	
	

	public static void main( String[] args ) throws CorruptIndexException, IOException
    {
        String indexDir = "index";
        String dataDir = "doc";
        
        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        try {
			numIndexed = indexer.index(dataDir,new TextFilesFilter());
		} finally{
			indexer.close();
		}
        
        long end = System.currentTimeMillis();
        
        System.out.println("Indexing "+numIndexed + " files took "+ (end-start)+" milliseconds");
        
        
        
    }

	
	
}
