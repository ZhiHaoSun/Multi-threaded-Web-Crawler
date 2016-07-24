
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.Thread;
import java.net.*;
import java.io.*;

class CrawlerThread extends Thread {
    private static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    private static final String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*\"?'?([^\"'>\\s]*)";
    private static HashMap<String, Boolean> mp = new HashMap<String, Boolean>();
    private static List<String> results = new ArrayList<String>();

    public static void setFirstUrl(String url) {
        try {
            queue.put(url);
        } catch (InterruptedException e) {
            // e.printStackTrace(); 
        }
    }

    public static List<String> getResults() {
        return results;
    }
    
    public List<String> parseUrls(String content) {
        List<String> links = new ArrayList<String>();
        Pattern pattern = Pattern.compile(HTML_HREF_TAG_PATTERN, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        String url = null;
        while (matcher.find()) {
            url = matcher.group(1);
            if (url.length() == 0 || url.startsWith("#"))
                continue;
            links.add(url);
        }
        return links;
    }

    @Override
    public void run() {
        while (true) {
            String url = "";
            try {
                url = queue.take();
            } catch (Exception e) {
                // e.printStackTrace(); 
                break;
            }

            String domain = "";
            URL netUrl = null;
            try {
                netUrl = new URL(url);
                domain = netUrl.getHost();
            } catch (MalformedURLException e) {
                // e.printStackTrace(); 
            }
            
            if (!mp.containsKey(url)) {
                mp.put(url, true);
                results.add(url);
                
                List<String> urls;
				try {
					StringBuffer buffer = new StringBuffer();
					String inputLine = null;
					URLConnection yc = netUrl.openConnection();
					BufferedReader in = new BufferedReader(
					        new InputStreamReader(yc.getInputStream()));
					
					while((inputLine = in.readLine()) != null) {
						buffer.append(inputLine);
						System.out.println(inputLine);
					}
					
					in.close();
					
					String content = buffer.toString();
					urls = parseUrls(content);
					
					  for (String u : urls) {
	                    try {
	                        queue.put(u);
	                    } catch (InterruptedException e) {
	                         e.printStackTrace(); 
	                    }
	                }
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            }
        }
    }
}

public class Main {
	
	public static void main(String[] args) {
		List<String> result = crawler("https://www.wikipedia.org");
		
		for(String url : result) {
			System.out.println(url);
		}
		
		System.out.println("Finished");
	}
	
    /**
     * @param url a url of root page
     * @return all urls
     */
    public static List<String> crawler(String url) {
        // Write your code here
        CrawlerThread.setFirstUrl(url);
        CrawlerThread[] thread_pools = new CrawlerThread[15];
        for (int i = 0; i < 15; ++i) {
            thread_pools[i] = new CrawlerThread();
            thread_pools[i].start();
        }
        
        try {
            Thread.sleep(900);
        } catch (InterruptedException e){
            // e.printStackTrace();
        }

        for (int i = 0; i < 15; ++i) {
            //thread_pools[i].interrupt();
            thread_pools[i].stop();
        }
        
        List<String> results = CrawlerThread.getResults();
        return results;
    }
}