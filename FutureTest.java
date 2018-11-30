package future;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Future test
 * @author One
 */
public class FutureTest 
{

	public static void main(String[] args) 
	{
		Scanner in = new Scanner(System.in);
		System.out.print("Enter the path (e.g. C:\\directory): ");
		String directory = in.nextLine();
		System.out.print("Enter the word you want to find (e.g user): ");
		String keyword = in.nextLine();
		
		MatchCounter counter = new MatchCounter(new File(directory), keyword);
		FutureTask<Integer> task = new FutureTask<>(counter);
		Thread t = new Thread(task);
		t.start();
		try {
			System.out.println("Number of files found in this word is: " + task.get() + ".");
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		in.close();
	}

}
/**
 * A task that counts files in the directory and its subdirectories 
 * that contain the given keyword
 */
 class MatchCounter implements Callable<Integer>
 {
	 private File directory;
	 private String keyword;
	 
	 /**
	  * Creates object class MatchCounter
	  * @param directory The directory from which you want to start searching.
	  * @param keyword Searched word
	  */
	 public MatchCounter(File directory, String keyword)
	 {
		 this.directory = directory;
		 this.keyword = keyword;
	 }
	 
	 public Integer call()
	 {
		int count = 0;
		 try 
		 {
			File[] files = directory.listFiles();
			List<Future<Integer>> results = new ArrayList<>();
			
			for(File file : files)
				if (file.isDirectory()) {
					MatchCounter counter = new MatchCounter(file, keyword);
					FutureTask<Integer> task = new FutureTask<>(counter);
					results.add(task);
					Thread t = new Thread(task);
					t.start();
				} else {
					if(search(file)) count++;
				}
			
			for (Future<Integer> result : results)
				try {
					count += result.get();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
		 } catch (InterruptedException e) {
			 e.printStackTrace();
		 }
		 return count;
	 }
	 
	 /**
	  * Searches a file to find a given keyword.
	  * @param file File to search.
	  * @return True if the file contains the key words.
	  */
	 public boolean search(File file)
	 {
		 try 
		 {
			try (Scanner in = new Scanner(file)) {
				boolean found = false;
				while(!found && in.hasNextLine()) {
					String line = in.nextLine();
					if(line.contains(keyword)) found = true;
				}
				return found;
			}
		 } catch (IOException e) {
			 return false;
		 }
	 }
 }
