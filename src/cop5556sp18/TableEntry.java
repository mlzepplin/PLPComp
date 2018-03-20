package cop5556sp18;

import cop5556sp18.AST.Declaration;

/**
 * Created by rishabh on 19/03/18.
 */
class TableEntry{
    public int scopeNum;
    public Declaration dec;
    TableEntry(){
        scopeNum=0;
        dec=null;
    }
    TableEntry(int s,Declaration d){
        scopeNum = s;
        dec = d;
    }

}