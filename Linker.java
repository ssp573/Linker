import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class Linker {
	public static void main(String[] args){
			int arr[]=new int[20];
			HashMap<String, Integer> symTable = new HashMap<String, Integer>();		//Initializing symbol table symTable
			InputStream is= System.in;		//Using an InputStream object for reading the input 
			is.mark(0);						//marking the start of the input stream to reset for the second pass
			arr = fPass(symTable,is);
			try {
				is.reset();					//resetting after the first pass
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String exc1=new String();
			sPass(arr,symTable,is,exc1);
			System.out.print(exc1);
		}
	
	private static int[] fPass(HashMap<String, Integer> symtable, InputStream is){
		
		Scanner s=new Scanner(is);
		int NM=s.nextInt();
		int arr[]=new int[NM];
		String symtab=new String();
			int size=0;
			for(int i=0; i<NM; i++){
				HashMap<String, Integer> SymsThisMod= new HashMap<String,Integer>();		//HashMap SymsThisMod to store symbols defined in the current module
				arr[i]=size;											//arr is to store the base addresses of all modules. size is for size till now.
				int ND = s.nextInt();
				while (ND>0){
					String symname=s.next();
					int loc=s.nextInt();
					if (!symtable.containsKey(symname)){		//if it is a new symbol
						symtable.put(symname, loc+size);		//storing absolute address as address given in the definition(loc)+base address of the module(size) 
						SymsThisMod.put(symname, loc+size);
					}
					else{
						System.out.println("Variable "+symname+" is multiply defined. First value used.");
					}
					ND--;
				}
				int NU= s.nextInt();
				while(NU>0){
					s.next();
					NU--;
				}
				int NL=s.nextInt();
				for (Map.Entry<String,Integer> entry : SymsThisMod.entrySet())
	            {
					if (entry.getValue()<=size+NL){			//if address of the symbol does not exceed the module size
						symtab=symtab+entry.getKey() +
	                             " = " + entry.getValue()+"\n";
						}
					else{
						entry.setValue(size);
						symtab=entry.getKey() +" = " + entry.getValue()+" Error: Address in definition for "
						+entry.getKey()+" exceeds the size of module; treating as 0 (relative).\n";
					}
					}
				size=size+NL;
				while(NL>0){
					s.next();
					s.next();
					NL--;
				}
			}
			
			if (NM==0)
			{
				System.out.println("Number of modules given to be 0. Thus considered to be no input");
				return arr;
			}
			else{
				System.out.println("\nSymbol Table");
				System.out.println(symtab);
				return arr;
				}
		
	}
	private static void sPass(int[] arr,HashMap<String, Integer> symtable, InputStream is, String exc1){
		Scanner sc=new Scanner(is);
		int NM=sc.nextInt();
		Set<String> usedsymbols = new HashSet<String>();	//set to identify symbols used in the whole program
		Set<String> symsusedinmod = new HashSet<String>();	//set to identify symbols used in the current module
		int address=0;
		boolean flag=true;
		if (NM>0){
			String exc= new String();
			int j=0;
			for(int i=0; i<NM; i++){
				//int base=arr[i];
				ArrayList<String> uses=new ArrayList<String>();		//array to store all uses
				int ND = sc.nextInt();
				while (ND>0){
					sc.next();
					sc.nextInt();
					ND--;
				}
				int NU= sc.nextInt();
				while(NU>0){
					String sym=sc.next();
					usedsymbols.add(sym);		//adding the symbol to the set usedsymbols
					uses.add(sym);				//adding to the array uses
					NU--;
				}
				
				int NL=sc.nextInt();
				int Modsize= NL;			//storing size of module in Modsize
				
				while(NL>0){
					if (flag){
						System.out.println("\nMemory Map");
						flag=false;
					}
						
					String Type= sc.next();
					address=sc.nextInt();
					switch(Type){
					case "A":					//If address is Absolute, checking is the address exceeds Machine size
						if (address%1000>200){
						System.out.println(j+": "+(address-address%1000)+" Error: Absolute address exceeds machine size; zero used.");
					}
					else{
								System.out.println(j+": "+address);}
								j++;
								break;
					case "I": System.out.println(j+": "+address);		//If address is Immediate
								j++;
								break;
					case "R": 			//If Relative, check for if the address exceeds size of module 'Modsize'
						
						if (address%1000>Modsize){
							System.out.println(j+": "+(address-address%1000)+" Error: Relative address exceeds module size; zero used.");
						}
						else{
							System.out.println(j+": "+(address+arr[i]));
							}
								j++;
								break;
					case "E": if (address%1000<uses.size()){
						if (symtable.containsKey(uses.get(address%10)))  //if symbol table contains the symbol used
						{
							System.out.println(j+": "+(symtable.get(uses.get(address%10))+(address-address%10)));
							symsusedinmod.add(uses.get(address%10));
						}
					else if(!symtable.containsKey(uses.get(address%1000))){
						System.out.println(j+": "+(address-address%10) +" Error: "+(uses.get(address%10))+" is not defined;zero used.");
					}}
					else {
						System.out.println(j+": "+address+" Error: External address exceeds length of use list; treated as immediate.");
					}	//if the last digit of the address which gives the index to the use-list is greater than the size of use-list
				
								j++;
								break;
					}
					
					NL--;
				}
				if (!uses.isEmpty()){for (String item:uses){
					if (!symsusedinmod.contains(item) && symtable.containsKey(item)){
						if (i==(Integer.parseInt(getModule(item,arr,symtable))-1)){
							exc="Warning: In module "+(Integer.parseInt(getModule(item,arr,symtable))-1)+", "+item+" appeared in the use-list but was not actually used.";
						}
					}
				}	
			}
			}
			System.out.println(exc);
		}
		else
		{
			sc.close();
			return;
		}
		
		
		if (usedsymbols!=symtable.keySet()){
			Set<String> symsdefined=symtable.keySet();
			Set<String> temp= new HashSet<String>(usedsymbols);
			for (String element:symsdefined){
				if (temp.add(element)){
					System.out.println("Warning: "+element+" was defined in module "+getModule(element,arr,symtable)+" but never used.\n");
				} //if we can add an element to temp, then it means it was not in 'usedsymbols'. Thus we know that the symbol was defined but not used.
			}
		}
		sc.close();
	}
	
	//function to get the module number.
	private static String getModule(String symbol, int[] arr, HashMap<String, Integer> symtable){
		for (int i=0;i<arr.length-1;i++){
			if (symtable.get(symbol)>=arr[i] && symtable.get(symbol)<arr[i+1] ){
				return String.valueOf(i);
			}
		}
		return String.valueOf(arr.length-1);
	}
}