import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Linker {

	public static void main(String[] args) throws IOException {
		// read file and put all text in one string
		File file = new File(args[0]);
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder stored = new StringBuilder();
		String s;

		ArrayList<String> varLst = new ArrayList<String>();
		ArrayList<String> varUsedLst = new ArrayList<String>();

		while ((s = br.readLine()) != null) {
			stored.append(s.trim() + " ");
		}

		// isolate string elements into an array
		String storedString = stored.toString();
		String[] arr = storedString.split("\\s+");

		// runTime: how many object modules
		int runTime = Integer.parseInt(arr[0]);
		// currAdd: base address for module
		int currAdd = 0;
		// iterator to move through array
		int start = 1;
		// dictionary for storing variables
		Map<String, Integer> map = new HashMap<String, Integer>();
		// store indices of variables for E address later
		ArrayList<String> arrLst = new ArrayList<String>();
		Map<String, Integer> moduleMap = new HashMap<String, Integer>();
		Map<String, Integer> errorMap = new HashMap<String, Integer>();
		ArrayList<String> finalLst = new ArrayList<String>();

		// pass 1
		int temp = 1;
		for (int i = 0; i < runTime; i++) {
			int lineLength = Integer.parseInt(arr[start]);
			// use temp variable to get to the number of modules
			temp += 1 + (2 * Integer.parseInt(arr[temp]));
			temp += 1 + Integer.parseInt(arr[temp]);

			start += 1;
			for (int j = 0; j < lineLength; j++) {
				// begin to input variables into dictionary
				if (!map.containsKey(arr[start])) {
					// compare variable definition to size of module
					if (Integer.parseInt(arr[start + 1]) > Integer.parseInt(arr[temp])) {
						finalLst.add("Error: In module " + i + " the def of " + arr[start] + " exceeds the module size; zero (relative) used.");
						map.put(arr[start], currAdd);
					} else {
						map.put(arr[start], currAdd + Integer.parseInt(arr[start + 1]));
					}
				}
				// use moduleMap and errorMap to error check
				if (!moduleMap.containsKey(arr[start])) {
					moduleMap.put(arr[start], i);
				} else {
					errorMap.put(arr[start], 1);
				}
				varLst.add(arr[start]);
				start += 2;
			}

			// increment through rest of module
			int restOfModule = Integer.parseInt(arr[start]);
			for (int j = 1; j < restOfModule + 1; j++) {
				varUsedLst.add(arr[start + j]);
			}
			start += (1 + restOfModule);

			int tracker = Integer.parseInt(arr[start]);
			currAdd += tracker;
			start += (1 + (2 * tracker));
			temp += 1 + (2 * Integer.parseInt(arr[temp]));
		}

		// display
		System.out.println("Symbol Table");
		// multiple definition check
		for (String elem : map.keySet()) {
			if (errorMap.containsKey(elem)) {
				System.out.println(elem + " = " + map.get(elem) + " Error: This variable is multiply defined; first value used.");
			} else {
				System.out.println(elem + " = " + map.get(elem));
			}
		}

		// undeclared variable check
		ArrayList<String> undeclared = new ArrayList<String>();
		boolean contains = false;
		for (String elem1 : varUsedLst) {
			for (String elem2 : varLst) {
				if (elem1.equals(elem2)) {
					contains = true;
					break;
				}
			}
			if(!contains) {
		        undeclared.add(elem1);
		    } else {
		    		contains = false;
		    }
		}

		// prepare second pass
		int memIter = 0;
		currAdd = 0;
		start = 1;
		boolean sentinel = false;
		System.out.println();
		System.out.println("Memory Map");

		// pass 2
		for (int i = 0; i < runTime; i++) {
			int lineLength = Integer.parseInt(arr[start]);
			start += (1 + (2 * lineLength));

			// get list of variables used
			int restOfModule = Integer.parseInt(arr[start]);
			for (int j = 1; j < restOfModule + 1; j++) {
				arrLst.add(arr[j + start]);
			}
			start += (1 + restOfModule);

			int modSize = start;
			int tracker = Integer.parseInt(arr[start]);

			// check letter conditions
			for (int j = 0; j < tracker; j++) {
				System.out.print(memIter + ": ");
				memIter += 1;
				if (arr[start + 1].equals("I")) {
					System.out.println(arr[start + 2]);
					start += 2;
				} else if (arr[start + 1].equals("A")) {
					if (Integer.parseInt(arr[start + 2]) % 1000 >= 200) {
						System.out.println(((Integer.parseInt(arr[start + 2]) / 1000) * 1000) + " Error: Absolute address exceeds machine size; zero used.");
					} else {
						System.out.println(arr[start + 2]);
					}
					start += 2;
				} else if (arr[start + 1].equals("R")) {
					int remain = Integer.parseInt(arr[start + 2]) % 1000;
					if (remain >= Integer.parseInt(arr[modSize])) {
						System.out.println(((Integer.parseInt(arr[start + 2]) / 1000) * 1000) + " Error: Relative address exceeds module size; zero used.");
					} else {
						System.out.println(Integer.parseInt(arr[start + 2]) + currAdd);
					}
					start += 2;
				} else if (arr[start + 1].equals("E")) {
					int remain = Integer.parseInt(arr[start + 2]) % 1000;
					int div = Integer.parseInt(arr[start + 2]) / 1000;
					if (remain >= arrLst.size()) {
						System.out.println(Integer.parseInt(arr[start + 2]) + " Error: External address exceeds length of use list; treated as immediate.");
						sentinel = true;
					} else {
						if (undeclared.contains(arrLst.get(remain))) {
							System.out.println((div * 1000) + " Error: " + arrLst.get(remain) + " is not defined; zero used.");
						} else {
							System.out.println((div * 1000) + map.get((arrLst.get(remain))));
						}
					}
					start += 2;
				}
			}
			start += 1;
			currAdd += tracker;
		}

		// set warnings
		ArrayList<String> warningLst = new ArrayList<String>();
		contains = false;
		for (String elem1 : varLst) {
			for (String elem2 : varUsedLst) {
				if (elem1.equals(elem2)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
		        warningLst.add(elem1);
		    } else {
		    		contains = false;
		    }
		}

		// display warnings
		System.out.println();
		for (String elem : warningLst) {
			System.out.println("Warning: " + elem + " was defined in module " + moduleMap.get(elem) + " but never used.");
		}

		start = 1;
		// final list to print out
		ArrayList<String> finale = new ArrayList<String>();
		// special case for external addresses
		ArrayList<String> useE = new ArrayList<String>();
		// keep track of the unused variables and which module they're in
		Map<String, Integer> modTracker = new HashMap<String, Integer>();
		for (int i = 0; i < runTime; i++) {
			int lineLength = Integer.parseInt(arr[start]);
			start += 1 + (2 * lineLength);

			for (int j = 1; j < Integer.parseInt(arr[start]) + 1; j++) {
				if (!finale.contains(arr[j + start])) {
					finale.add(arr[j + start]);
					modTracker.put(arr[j + start], i);
				}
			}
			int restOfModule = Integer.parseInt(arr[start]);
			start += 1 + restOfModule;

			int tracker = Integer.parseInt(arr[start]);
			for (int j = 0; j < tracker; j++) {
				if (arr[start + 1].equals("E") && sentinel == false) {
					int r = Integer.parseInt(arr[start + 2]) % 1000;
					if (!useE.contains(finale.get(r))) {
						useE.add(finale.get(r));
					}
				}
				start += 2;
			}
			start += 1;
			finale.removeAll(useE);
			for (String elem : finale) {
				if (sentinel == false) {
					System.out.println("Warning: In module " + modTracker.get(elem) + ", " + elem + " appeared in the use list but was not actually used.");
				}
			}
			finale.clear();
			useE.clear();
			modTracker.clear();
		}

		// print the final values
		for (String elem : finalLst) {
			System.out.println(elem);
		}
	}
}
