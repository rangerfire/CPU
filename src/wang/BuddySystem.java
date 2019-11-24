package wang;

import java.util.ArrayList;
import java.util.Scanner;

public class BuddySystem {
	
	public static int max;
	public static int[] size ;
	public static int[] status;
	public static String[] pname ;
	public static int count = 0;
	//init
	public static void init()
	{
		size = new int[max *2];
		status = new int[max *2];
		pname = new String[max *2];

		for(int i=1;i<size.length;i++)
		{
			int order = (int)(Math.log((double)i)/Math.log((double)2));
			size[i] = (int) Math.pow(2, (int)(Math.log((double)max)/Math.log((double)2)) - order);
		}
		for(int i=1;i<status.length;i++)
		{
			status[i] = 0;
		}
		for(int i=1;i<pname.length;i++)
		{
			pname[i] = "";
		}
		
	}
	//do operation(allocation or return)
	public static void operate(String p, String op, int size)
	{
		int order = 0;
		//allocation
		if(op.equals("+"))
		{
			
			order = (int)(Math.log((double)size)/Math.log((double)2)) + 1;
			int need = (int)Math.pow(2, order);
			//System.out.println(need + ", " +order);
			find(need,p);
		}
		else
		//return
		{
			
			Return(p);
		}
	}
	//find and set status
	public static void find(int need,String p)
	{
		for(int i=1;i<size.length;i++)
			if(size[i] == need && status[i] == 0)
			{
				set1_up(i);
				set1_down(i);
				pname[i] = p;
				return;
			}
	}
	public static void set1_up(int index)
	{
		if(index >=1)
		{
			status[index] = 1;
			index = index/2;
			set1_up(index);
		}
		else
			return;
	}
	public static void set1_down(int index)
	{
		if(index<=size.length-1)
		{
			status[index] = 1;
			int index1 = index*2;
			int index2 = index*2 + 1;
			set1_down(index1);
			set1_down(index2);
		}
		else
			return;
	}
	public static void Return(String p)
	{
		int index = 0;
		for(int i=1;i<pname.length;i++)
			if(pname[i].equals(p))
				index = i;
		set0_up(index);
		set0_down(index);
	}
	public static void set0_up(int index)
	{
		if(index >= 1)
		{
			status[index] = 0;
			if(index%2 == 0)//even
			{
				if(status[index+1] == 0)
					set0_up(index/2);	
				else
					return;
			}
			else
			{
				if(status[index-1] == 0)
					set0_up(index/2);
				else
					return;
			}
		}
		else
			return;
	}
	public static void set0_down(int index)
	{
		if(index<=size.length-1)
		{
			status[index] = 0;
			int index1 = index*2;
			int index2 = index*2 + 1;
			set0_down(index1);
			set0_down(index2);
		}
		else
			return;
	}
	public static void output()
	{
		System.out.println("State of memory:");
		if(status[1] == 0)
		{
			System.out.println("------------------------------------------------------------------");
			System.out.println("|                           2048K                                |");
			System.out.println("------------------------------------------------------------------");
		}
		else
		{
			output_r_forcount(2);
			
			for(int i=0;i<count+1;i++)
				System.out.print("-");
			System.out.println("");
			
			System.out.print("|");
			output_r(2);
			
			System.out.println("");
			
			for(int i=0;i<count+1;i++)
				System.out.print("-");
			System.out.println("");
			count = 0;
		}
	}
	public static void output_r(int index)
	{
		//index is even
		if(index>=size.length)
			return;
		// 0 0
		if(status[index] ==0 && status[index+1] ==0)
			return;
		else
			// 1 0
			if(status[index] == 1 && status[index+1] == 0)
			{
				if(pname[index].equals(""))
					output_r(index*2);
				else
				{
					System.out.print(pname[index] + " " + size[index] + "K");
					int b = size[index]/32;
					for(int j=0;j<b;j++)
						System.out.print(" ");
					System.out.print("|");
				}
				System.out.print( size[index+1] + "K");
				int b = size[index]/32;
				for(int j=0;j<b;j++)
					System.out.print(" ");
				System.out.print("|");
				return;
			}
			else
				// 0 1
				if(status[index] == 0 && status[index+1] == 1)
				{
					if(pname[index+1].equals(""))
					{
						System.out.print(size[index] + "K");
						int b = size[index]/32;
						for(int j=0;j<b;j++)
							System.out.print(" ");
						System.out.print("|");
						output_r( (index+1)*2 );
					}
					else
					{
						System.out.print(size[index] + "K");
						int b = size[index]/32;
						for(int j=0;j<b;j++)
							System.out.print(" ");
						System.out.print("|");
						System.out.print(pname[index+1] + " " + size[index+1] + "K");
						b = size[index]/32;
						for(int j=0;j<b;j++)
							System.out.print(" ");
						System.out.print("|");
						return;
					}
					System.out.print(size[index] + "K");
					int b = size[index]/32;
					for(int j=0;j<b;j++)
						System.out.print(" ");
					System.out.print("|");
				}
				else
				// 1 1
				{
					if(pname[index].equals("") && pname[index+1].equals("") )
					{
						output_r(index*2);
						output_r( (index+1)*2 );
					}
					else
						if(!pname[index].equals("") && pname[index+1].equals(""))
						{
							System.out.print(pname[index] + " " + size[index] + "K");
							int b = size[index]/32;
							for(int j=0;j<b;j++)
								System.out.print(" ");
							System.out.print("|");
							output_r( (index+1)*2 );
						}
						else
							if(pname[index].equals("") && !pname[index+1].equals(""))
							{
								output_r(index*2);
								System.out.print(pname[index+1] + " " + size[index+1] + "K");
								int b = size[index]/32;
								for(int j=0;j<b;j++)
									System.out.print(" ");
								System.out.print("|");
							}
							else
							{
								System.out.print(pname[index] + " " + size[index] + "K");
								int b = size[index]/32;
								for(int j=0;j<b;j++)
									System.out.print(" ");
								System.out.print("|");
								System.out.print(pname[index+1] + " " + size[index+1] + "K");
								b = size[index]/32;
								for(int j=0;j<b;j++)
									System.out.print(" ");
								System.out.print("|");
							}
				}
	}
	public static void output_r_forcount(int index)
	{
		//index is even
		if(index>=size.length)
			return ;
		// 0 0
		if(status[index] ==0 && status[index+1] ==0)
			return ;
		else
			// 1 0
			if(status[index] == 1 && status[index+1] == 0)
			{
				if(pname[index].equals(""))
					output_r_forcount(index*2);
				else
				{
				
					int b = size[index]/32;

					count = count + 4 + b + String.valueOf(size[index]).length();
				}
			
				int b = size[index]/32;
			
				count = count + 2 + b + String.valueOf(size[index]).length();
				return;
			}
			else
				// 0 1
				if(status[index] == 0 && status[index+1] == 1)
				{
					if(pname[index+1].equals(""))
					{
					
						int b = size[index]/32;
					
					
						count = count + 2 + b + String.valueOf(size[index]).length();
						output_r_forcount( (index+1)*2 );
					}
					else
					{
						
						int b = size[index]/32;
				
						count = count + 2 + b + String.valueOf(size[index]).length();
				
						b = size[index]/32;
				
						count = count + 4 + b + String.valueOf(size[index]).length();
						return;
					}
				
					int b = size[index]/32;
				
					count = count + 2 + b + String.valueOf(size[index]).length();
				}
				else
				// 1 1
				{
					if(pname[index].equals("") && pname[index+1].equals("") )
					{
						output_r_forcount(index*2);
						output_r_forcount( (index+1)*2 );
					}
					else
						if(!pname[index].equals("") && pname[index+1].equals(""))
						{
							
							int b = size[index]/32;
						
							count = count + 4 + b + String.valueOf(size[index]).length();
							output_r_forcount( (index+1)*2 );
						}
						else
							if(pname[index].equals("") && !pname[index+1].equals(""))
							{
								output_r_forcount(index*2);
							
								int b = size[index]/32;
						
								count = count + 4 + b + String.valueOf(size[index]).length();
							}
							else
							{
							
								int b = size[index]/32;
						
								count = count + 4 + b + String.valueOf(size[index]).length();
		
								b = size[index]/32;
							
								count = count + 4 + b + String.valueOf(size[index]).length();
							}
				}
	}
	
	//main frame
	public static void startBuddy()
	{
		try {
		System.out.println("Please insert your operation or 'quit' for quit:");
		Scanner sc = new Scanner(System.in);
		String temp = sc.nextLine();
		if(temp.equals("quit"))
		{
			System.out.println("Bye bye!");
			return;
		}
		else
		{
			//
			
			String p = temp.charAt(0) + "";
			String op = temp.charAt(2) + "";
			String s = "";
			for(int i=3;i<temp.length()-1;i++)
			{
				s = s + temp.charAt(i);
			}
			int size = Integer.parseInt(s);
			operate(p, op, size);
			output();
			startBuddy();
			
		}
		}catch(Exception e)
		{
			System.out.println("please insert right operation!");
			startBuddy();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("-------------------------Welcome to Buddy System---------------------------");
		System.out.println("Please insert the size of memory (megabytes):");
		Scanner sc = new Scanner(System.in);
		String l = sc.nextLine();
		max = Integer.parseInt(l)*1024;
		init();
		output();
		startBuddy();
	}

}

