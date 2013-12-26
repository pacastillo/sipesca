package pete.android.study;

import java.net.URL;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HtmlCleanerStudyActivity extends Activity {
	
	// HTML page
	static final String BLOG_URL = "http://xjaphx.wordpress.com/";
	// XPath query
	static final String XPATH_STATS = "//div[@id='blog-stats']/ul/li";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// init view layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // decide output
        String value = "";
        try {
        	value = getBlogStats();
        	((TextView)findViewById(R.id.tv)).setText(value);
        } catch(Exception ex) {
        	((TextView)findViewById(R.id.tv)).setText("Error");
        }
    }
    
    /*
     * get blog statistics
     */
    public String getBlogStats() throws Exception {
    	String stats = "";
    	
    	// config cleaner properties
    	HtmlCleaner htmlCleaner = new HtmlCleaner();
    	CleanerProperties props = htmlCleaner.getProperties();
    	props.setAllowHtmlInsideAttributes(false);
    	props.setAllowMultiWordAttributes(true);
    	props.setRecognizeUnicodeChars(true);
    	props.setOmitComments(true);
    	
    	// create URL object
    	URL url = new URL(BLOG_URL);
    	// get HTML page root node
    	TagNode root = htmlCleaner.clean(url);
    	
    	// query XPath
    	Object[] statsNode = root.evaluateXPath(XPATH_STATS);
    	// process data if found any node
    	if(statsNode.length > 0) {
    		// I already know there's only one node, so pick index at 0.
    		TagNode resultNode = (TagNode)statsNode[0];
    		// get text data from HTML node
    		stats = resultNode.getText().toString();
    	}
    	
    	// return value
    	return stats;
    }
}