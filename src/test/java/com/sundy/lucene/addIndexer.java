package com.sundy.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class addIndexer {

	
	private String[] ids = {"1","2"};
	private String[] unindexed = {"Netherlands","Italy"};
	private String[] unstored = {"Amsterdam has lots of bridges","Venice has lots of canals"};
	private String[] text = {"Amsterdam","Venice"};
	
	private Directory directory;
	
	
	private IndexWriter getWriter() throws CorruptIndexException, LockObtainFailedException, IOException {
		return new IndexWriter(directory, new WhitespaceAnalyzer(),IndexWriter.MaxFieldLength.UNLIMITED);
//		return new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_30),IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	@Before
	public void setUp() throws CorruptIndexException, LockObtainFailedException, IOException{
		directory = new RAMDirectory();
		IndexWriter indexWriter = getWriter();
		for(int i=0; i<ids.length; i++){
			Document doc = new Document();
			doc.add(new Field("id", ids[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
			doc.add(new Field("country", unindexed[i], Field.Store.YES, Field.Index.NO));
			doc.add(new Field("contents", unstored[i], Field.Store.NO, Field.Index.ANALYZED));
			doc.add(new Field("city", text[i], Field.Store.YES, Field.Index.ANALYZED));
			indexWriter.addDocument(doc);
		}
		indexWriter.close();
	}
	
	public int getHitCount(String fieldName,String q) throws IOException{
		IndexSearcher indexSearcher = new IndexSearcher(directory);
		Term t = new Term(fieldName, q);
		Query query = new TermQuery(t);
		int hitCount = hitCount(indexSearcher,query);
		indexSearcher.close();
		return hitCount;
	}

	public static int hitCount(IndexSearcher searcher, Query query) throws IOException {
		return searcher.search(query, 1).totalHits;
	}
	
	@Test
	public void testIndexWriter() throws CorruptIndexException, LockObtainFailedException, IOException{
		IndexWriter writer = getWriter();
		System.out.println(ids.length);
		System.out.println(writer.numDocs());
		writer.close();
	}
	
	@Test
	public void testReader() throws CorruptIndexException, IOException{
		IndexReader reader = IndexReader.open(directory);
		System.out.println(ids.length);
		System.out.println(reader.maxDoc());
		System.out.println(reader.numDocs());
		reader.close();
	}
	
	@Test
	public void testDeleteBeforeOptimize() throws CorruptIndexException, LockObtainFailedException, IOException{
		IndexWriter writer = getWriter();
		System.out.println(ids.length);
		System.out.println(writer.numDocs());
		writer.deleteDocuments(new Term("id", "1"));
		writer.commit();
		System.out.println(writer.hasDeletions());
		System.out.println(writer.maxDoc());
		System.out.println(writer.numDocs());
		writer.close();
	}
	
	@Test
	public void testDeleteAfterOptimize() throws CorruptIndexException, LockObtainFailedException, IOException{
		IndexWriter writer = getWriter();
		System.out.println(ids.length);
		System.out.println(writer.numDocs());
		writer.deleteDocuments(new Term("id", "1"));
		writer.optimize();//合并优化索引
		writer.commit();
		System.out.println(writer.hasDeletions());
		System.out.println(writer.maxDoc());
		System.out.println(writer.numDocs());
		writer.close();
	}
	
	@Test
	public void testUpdate() throws IOException{
		System.out.println(getHitCount("city", "Amsterdam"));
		System.out.println(getHitCount("city", "Venice"));
		IndexWriter writer = getWriter();
		Document doc = new Document();
		doc.add(new Field("id", "1", Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("country", "Netherlands", Field.Store.YES, Field.Index.NO));
		doc.add(new Field("contents", "Den Haag has a lot of museums", Field.Store.NO, Field.Index.ANALYZED));
		doc.add(new Field("city", "Den Haag", Field.Store.YES, Field.Index.ANALYZED));
		writer.updateDocument(new Term("id","1"), doc);
		writer.close();
		System.out.println(getHitCount("city", "Amsterdam"));
		System.out.println(getHitCount("city", "Venice"));
		System.out.println(getHitCount("city", "Den Haag"));
		System.out.println(getHitCount("city", "Den"));
		System.out.println(getHitCount("contents", "museums"));
	}
	
	
	
	
	
	

	
	
}
