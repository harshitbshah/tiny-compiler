package cop5556fa17;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import cop5556fa17.AST.Declaration;

public class SymbolTable {

	// TODO add fields
	HashMap<String, Declaration> symbolTable;
	Stack<Integer> scopeStack;

	/**
	 * to be called when block entered
	 */

	public SymbolTable() {
		// TODO: IMPLEMENT THIS
		symbolTable = new HashMap<String, Declaration>();
	}

	public boolean insert(String ident, Declaration dec) {
		// TODO: IMPLEMENT THIS

		if (symbolTable.containsKey(ident)) {
			return false;

		} else {
			symbolTable.put(ident, dec);
		}

		return true;
	}

	public Declaration lookup(String ident) {
		// TODO: IMPLEMENT THIS
		Declaration returnDec = null;

		if (symbolTable.containsKey(ident)) {
			returnDec = symbolTable.get(ident);
		}

		return returnDec;
	}

//	@Override
//	public String toString() {
//		// TODO: IMPLEMENT THIS
//		StringBuilder sb = new StringBuilder();
//		Iterator<String> itr = symbolTable.keySet().iterator();
//		String description = "";
//
//		while (itr.hasNext()) {
//			String key = itr.next();
//			HashMap<Integer, Declaration> map = symbolTable.get(key);
//			Iterator<Integer> itrMap = map.keySet().iterator();
//
//			while (itrMap.hasNext()) {
//				int keyInt = itrMap.next();
//				Declaration dec = map.get(keyInt);
//				sb.append("Identifier: " + key);
//				sb.append("Scope: " + keyInt);
//				sb.append("Attribute Dec: " + dec.toString());
//				description = sb.toString();
//			}
//
//		}
//
//		return description;
//	}

}
