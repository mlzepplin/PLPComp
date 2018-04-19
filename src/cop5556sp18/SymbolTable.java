package cop5556sp18;

import cop5556sp18.AST.Declaration;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by rishabh on 19/03/18.
 */

public class SymbolTable {

    public HashMap<String,Vector<TableEntry>> table=new HashMap<String,Vector<TableEntry>>();
    public Stack<Integer> scopeStack = new Stack<Integer>();
    public int currentScope =0;
    public int nextScope=0;

    public void SymbolTable(){
        //table = new HashMap<String,Vector<TableEntry>>();
        //scopeStack = new Stack<Integer>();
        //currentScope = 0;
        //nextScope=0;
    }
    public void insertEntry(Declaration dec){
        if(table.containsKey(dec.name)){
            //chain
            table.get(dec.name).add(new TableEntry(currentScope,dec));
        }
        else{
            Vector<TableEntry> temp = new Vector<TableEntry>();
            temp.add(new TableEntry(currentScope,dec));
            table.put(dec.name,temp);
        }
    }
    public void enterScope(){
        currentScope = nextScope++;
       // System.out.println("entered scope: "+currentScope);
        scopeStack.push(currentScope);
    }
    public void leaveScope(){
        //copeStack.pop();
        currentScope = scopeStack.pop();
       // System.out.println("exited scope, now current: "+currentScope);
    }

    public Declaration lookup(String name){
        if(table.containsKey(name)){
            //search chain from most recently added to least recently added
            Vector<TableEntry> interestVector = table.get(name);
            int lastValidIndex = interestVector.size()-1;
            TableEntry currentEntry;
            TableEntry temp = null;
            while(lastValidIndex>=0){
                //take the last valid element, check if in scope stack
                currentEntry = interestVector.get(lastValidIndex);

                //if yes, break out, found the best
                if(scopeStack.search(currentEntry.scopeNum)!=-1){
                    //entry exists
                    temp = currentEntry;
                    break;
                }
                //if not , then check for lesser
                lastValidIndex--;

            }
            if(temp==null){/*DIDN'T FIND ANY DECLARATION BEFORE USE!!*/
            return null;}

            return temp.dec;
        }
        else return null;
    }

    public boolean hasNameClash(String name){
        //int currentScope = scopeStack.peek();
        Vector<TableEntry> v = table.get(name);
        if(v==null) return false;
        for(int i=v.size()-1;i>=0;i--){
            if(v.get(i).scopeNum == currentScope) return true;
        }
        return false;
    }

}
