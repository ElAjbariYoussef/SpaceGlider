public class Main{

    public static void main(String[] args){

        String option = args[0];
        System.out.println(option);
        if (option.equals("-c")){
            System.out.print("compressing ");
            String filepath = args[1];
            System.out.println(filepath);
            String output = args[2];
            Compress.compress(filepath,output);
        }
        else if (option.equals("-e")){
            String filepath = args[1];
            String output = args[2];
            System.out.println("Extracting "+filepath+" to "+output);
            Extract.extract(filepath,output);
        }
        else{
            System.out.println("space-glider [-option]");
            System.out.println("OPTIONS :");
            System.out.println("-c filepath directory       Compresses file from directory");
            System.out.println("-e filepath directory       Extracts file to directory");
            
        }
    }
}