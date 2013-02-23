package zee.engine.parser;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * Given key "f(x,y)=x+y", symbol is f, arguments are [x,y], definition is x+y,
 * and signature is f(x,y) with no spaces.
 *
 */
public final class EquationSet {
   
   private Vector<String> symbols = new Vector<String>();
   private Hashtable<String,String[]> arguments = new Hashtable<String,String[]>();
   private Hashtable<String,String> definitions = new Hashtable<String,String>();
   private Hashtable<String,Hashtable<String,String>> meta;
   
   /** Creates a new instance of EquationSet */
   public EquationSet() {
      // map symbols to metadata
      meta = new Hashtable<String,Hashtable<String,String>>();
   }
   
   public EquationSet(Hashtable<String,String> symbolsAndDefs) throws ParseException {
      for(String key : symbolsAndDefs.keySet())
         addSymbol(key, symbolsAndDefs.get(key));
      meta = new Hashtable<String,Hashtable<String,String>>();
   }

   public Vector<String> getAllDomainVariables() {
      TreeSet<String> d = new TreeSet<String>();
      for(String symbol : symbols) {
         String[] args = arguments.get(symbol);
         for(int i=0; i < args.length; i++)
            d.add(args[i]);
      }
      return new Vector<String>(d);
   }

   public Vector<String> getAllSignatures() {
      Vector<String> sigs = new Vector<String>();
      for(String name : symbols)
         sigs.add(getSignature(name));
      return sigs;
   }
   
   /**
    * returns false if name is null or if name is not contained in the symbol
    * list.
    */
   public boolean isSymbolDefined(String name) {
      if(name == null)
         return false;
      return symbols.contains(name);
   }
   
   public Vector<String> getAllSymbols() {
      return new Vector<String>(symbols);
   }
   
   /**
    * reconstructs the signature of a function which was added.
    * If a function was added without arguments (ie it's a constant)
    * then just the function name is returned.
    */
   public String getSignature(String symbol) {
      StringBuffer signature = new StringBuffer();
      String retVal = null;
      signature.append(symbol);
      String[] args = getArguments(symbol);
      if(args.length > 0) {
         signature.append("(");
         for(int s = 0; s < args.length; s++)
            signature.append(args[s]+",");
         signature.append(")");
         retVal = signature.toString().replace(",)", ")");
      }
      else {
         retVal = signature.toString();
      }
      
      return retVal;
   }
   
   /**
    * after calling addSymbol("f(x,y)","x+y") ,
    * then getArguments("f") returns ["x" "y"]
    * after calling addSymbol("a","1") ,
    * then getArguments("a") returns String[0]
    */
   public String[] getArguments(String symbol) {
      return arguments.get(symbol);
   }
   
   /**
    * after calling addSymbol("f(x,y)","x+y") ,
    * then getDefinition("f") returns "x+y"
    * if symbol is not in this set, returns null
    */
   public String getDefinition(String symbol) {
      return definitions.get(symbol);
   }
   
   /**
    * If there's no metadata for this symbol,
    * return an empty table.
    * 
    * Metadata is a set of key/value strings for each function
    * 
    * @param symbol
    * @return a hashtable of metadata
    */
   public Hashtable<String,String> getMetadata(String symbol) {
      Hashtable<String,String> values = meta.get(symbol);
      if( values == null)
          values = new Hashtable<String,String>();
      return values;
   }
   
   public String getMetadata(String symbol, String key) {
      String value = null;
      Hashtable<String,String> data = meta.get(symbol);
      if(data != null)
         value = data.get(key);
      return value;
   }
   
   public void addSymbol(   String signature,
                            String def, 
                            Hashtable<String,String> metaData) throws ParseException 
   {
      
      String symbol = MathString.getFunctionName(signature.trim());
      if( ! symbols.contains(symbol)) {
         addSymbol(signature,def);
         if(metaData != null)
         {
            meta.put(symbol, metaData);
            if(MathString.isMatrix(def))
                addNumericFunctionMetadata(signature);
         }
      }
      
   }

   
   /**
    * addSymbol("f(x,y)","x+y") adds the symbol "f" with arguments ["x" "y"]
    * and definition "x+y". 
    * 
    * addSymbol("a", "1") adds the symbol "a" with arguments "" and
    * definition "1"
    *
    * if the symbol is already present in this equation set, this method fails
    */
   public void addSymbol(String signature, String def) throws ParseException {

      signature = signature.trim();
      
      // default if it has no function arguments, for example, "a"
      String sym = MathString.getFunctionName(signature);
      String[] argArray = new String[0];

      // if it has function arguments, for example, "f(x,y)"
      if(signature.indexOf("(") != -1)
         argArray = MathString.getFunctionArgs(signature);
      
      // check that the argument names don't overlap a function name
      List<String> argList = Arrays.asList(argArray);
      Set<String> intersection = new HashSet<String>(symbols);
      intersection.retainAll(argList);
      if( intersection.size() == 1) {
         String msg = sym + " has an signature argument " + intersection 
                     + " which is already a defined function";
         throw new ParseException(msg,0);
      }
      if( intersection.size() > 1) {
         String msg = sym + " has arguments " + intersection 
                     + " in the signature which are already defined functions";
         throw new ParseException(msg,0);
      }
      
      
      // check that the new function name don't overlap an existing arg name
      if( getAllDomainVariables().contains(sym)) {
         String msg = sym + " can't be used as a function, "
                 + "it is already defined as a domain variable";
         throw new ParseException(msg,0);
      }

        if(MathString.isMatrix(def))
            addNumericFunctionMetadata(signature);

      
      // if symbol is already present, don't add it
      if( ! symbols.contains(sym)) {
         symbols.add(sym);
         arguments.put(sym,argArray);
         definitions.put(sym,def);
      }

   }

    private void addNumericFunctionMetadata(String signature) {

      signature = signature.trim();

      // default if it has no function arguments, for example, "a"
      String sym = MathString.getFunctionName(signature);
      String[] argArray = new String[0];

      // if it has function arguments, for example, "f(x,y)"
      if(signature.indexOf("(") != -1)
         argArray = MathString.getFunctionArgs(signature);

        Hashtable<String, String> symMeta = getMetadata(sym);
        if (argArray.length == 1) {
            symMeta.put(MatrixParser.INDEPENDANT_VARIABLE, argArray[0]);
        }
        if (argArray.length == 2) {
            symMeta.put(MatrixParser.INDEPENDANT_VARIABLE_1, argArray[0]);
            symMeta.put(MatrixParser.INDEPENDANT_VARIABLE_2, argArray[1]);
        }
        meta.put(sym, symMeta);
    }
   
   
}
