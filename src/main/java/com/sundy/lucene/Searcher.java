package com.sundy.lucene;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

	public static void main(String[] args) throws IOException, org.apache.lucene.queryParser.ParseException {
		 String indexDir = "index";
	     String q = "api";
	     search(indexDir,q);
	}

	private static void search(String indexDir, String q) throws IOException, org.apache.lucene.queryParser.ParseException {
		Directory dir = FSDirectory.open(new File(indexDir));
		IndexSearcher is = new IndexSearcher(dir);
		QueryParser queryParser = new QueryParser(Version.LUCENE_30, "contents", new StandardAnalyzer(Version.LUCENE_30));
		long start = System.currentTimeMillis();
		Query query = queryParser.parse(q);
		TopDocs hits = is.search(query, 10);
		long end = System.currentTimeMillis();
		System.out.println("found "+hits.totalHits+" document(s)(in "+(end - start)+" milliseconds) that matched query '"+q+"':");
		for(ScoreDoc scoreDoc : hits.scoreDocs){
			Document doc = is.doc(scoreDoc.doc);
			System.out.println(doc.get("fullpath"));
		}
		is.close();
	}

}
