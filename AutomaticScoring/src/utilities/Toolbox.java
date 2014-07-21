package utilities;

import org.python.util.PythonInterpreter;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;


/***
 * it has some often used classes
 * 
 * @author miaochen
 *
 */
public class Toolbox {
	
	boolean tokenizerOn=false;
	boolean splitterOn=false;
	boolean taggerOn=false;
	boolean dbOn=false;
	boolean wnmatchOn=false;
	boolean pathOn=false;
	boolean linOn=false;
	boolean pyInterpOn=false; //python interpreter 
	
	SentenceTokenizer tokenizer = null;
	SentenceSplitter splitter = null;
	SentenceTagger tagger = null;
	DBConnection dbcon = null;
	WordNetMatching wnmatch=null;
	Path path=null;
	Lin lin=null;
	PythonInterpreter interp=null;
	
	public Toolbox () {
		
	}
	
	public Toolbox (boolean tokenizerOn, boolean splitterOn, boolean taggerOn, boolean dbOn, boolean wnmatchOn, boolean pathOn, boolean linOn) {
		this.tokenizerOn=tokenizerOn;
		this.splitterOn=splitterOn;
		this.taggerOn=taggerOn;
		this.dbOn=dbOn;
		this.wnmatchOn=wnmatchOn;
		this.pathOn=pathOn;
		this.linOn=linOn;		
	}
	
	public Toolbox (boolean tokenizerOn, boolean splitterOn, boolean taggerOn, boolean dbOn, boolean wnmatchOn
			, boolean pathOn, boolean linOn, boolean pyInterpOn) {
		this (tokenizerOn, splitterOn, taggerOn, dbOn, wnmatchOn, pathOn, linOn);
		this.pyInterpOn=pyInterpOn;
	}
	
	public void enableTools() {
		if (tokenizerOn) {
			tokenizer = new SentenceTokenizer(Params.tokenizerModel);
		}
		if (splitterOn) {
			splitter = new SentenceSplitter(Params.spliterModel);
		}
		if (taggerOn) {
			tagger = new SentenceTagger(Params.taggerModel, Params.taggerDict);
		}
		if (dbOn) {
			String pwd=PasswordField.readPassword("Enter password: ");
			System.out.println("Finished entering password");
			dbcon=new DBConnection(Params.dbUrl,Params.dbClass, Params.usr,pwd);
		}
		if (wnmatchOn) {
			wnmatch=new WordNetMatching(Params.wnpath);
		}
		if (pathOn) {
			JWS	ws = new JWS(Params.wndir, Params.wnversion);
			path=ws.getPath();
		}
		if (linOn) {
			JWS	ws = new JWS(Params.wndir, Params.wnversion);
			lin=ws.getLin();
		}
		if (pyInterpOn) {
			interp = new PythonInterpreter();
		}
	}
	
	public void setSentenceTokenizer (boolean setup) {
		tokenizerOn=setup;
	}
	
	public void setSentenceSplitter (boolean setup) {
		splitterOn=setup;
	}
	
	public void setSentenceTagger (boolean setup) {
		taggerOn=setup;
	}
	
	public void setDbConnection (boolean setup) {
		dbOn=setup;
	}
	
	public void setWnMatch (boolean setup) {
		wnmatchOn=setup;
	}
	
	public void setWnPath (boolean setup) {
		pathOn=setup;
	}
	
	public void setWnLin (boolean setup) {
		linOn=setup;
	}
	
	public void setPyInterp (boolean setup) {
		pyInterpOn=setup;
	}
	
	
	public static void main (String[] args) {
		
		Toolbox tools=new Toolbox(false,false,false,true,false,false,false);
		tools.enableTools();
		System.out.println(tools.dbcon==null);
		
	}

}
