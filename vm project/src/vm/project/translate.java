/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vm.project;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import jdk.nashorn.internal.parser.TokenType;

/**
 *
 * @author usuario
 */
public class translate
{
   public String vmCodeContent ="";
   private LinkedList<String> vmCode = new LinkedList<String>();
   int n;
   public boolean putInit = true;
   
   public translate(File[] path){
      try{
         ReadVmCode(path);
      }
      catch(Exception e){}
   }
   
   public String translateToHack(){
      n=0;
      String result="";
      for (int i = 0; i < vmCode.size(); i++)
      {
         String[] thisInstruction = vmCode.get(i).split("\\s+");
         
         switch(thisInstruction[0]){
            case "add": case "sub": case "neg": case "gt": case "eq": case "lt": case "and": case "or": case "not":
              result+= Arithmetic(thisInstruction);
               break;
            case "pop": case "push":
               result+=MemoryAccess(thisInstruction);
            break;
            case "label": case "goto": case "if-goto":
               result+=Flow(thisInstruction);
            break;
            case "function": case "call": case "return":
               result+=Calls(thisInstruction);
            break;
         }
         result+="\n";
      }
      return addInit()+"\n"+ result;
   }
    private void ReadVmCode(File[] files)throws  Exception{ 
       String function="";
       String fileName="";
       String label="";
       files = OrderTheFiles(files);
       for (int i = 0; i < files.length; i++)
       {
          fileName = files[i].getName().replace(".", "!").split("!")[0];
        BufferedReader codeFile = new BufferedReader(new FileReader(files[i]));
       
       String line;
         while ((line = codeFile.readLine()) != null){
            if(line.contains("//")){
               String noComents="";
               try{noComents = line.split("//")[0];}catch(Exception e){}
               
               if(!noComents.isEmpty()){
                  vmCode.add(line.split("//")[0].trim());
                  vmCodeContent+=line.split("//")[0].trim()+"\n";
                  continue;
               }
            }else{
               if(!line.isEmpty()){
                  if(line.contains("function")){ function = line.replace("function", "").trim();}
                  
                  if(line.contains("label") || line.contains("goto") || line.contains("if-goto")){
                     line = OverwriteLabels(function, line.trim().split(" ")[1]);
                  }else if(line.contains("static")){
                      line = OverwriteFunctions(fileName, line);
                  }
                  vmCode.add(line.split("//")[0].trim());
                  vmCodeContent+=line.split("//")[0].trim()+"\n";
                  continue;
               }
            }
            vmCodeContent+=line.trim()+"\n";
         }
       codeFile.close();  
       }
       
    }
    
    private String OverwriteFunctions(String fileName,String line){
       String[] parts = line.split("static");
       return parts[0].trim()+" static "+fileName + "."+parts[1].trim();
    }
    
    private String OverwriteLabels(String functionName, String line){
      return "label "+functionName + "$"+line;
    }
    
    //PRINT INSTRUCTIONS
    private String Arithmetic(String[] parts)
    {
       String result="";
       switch(parts[0]){
       case "add":
          result=
                  "@SP \n"+
                  "AM=M-1 \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "M=D+M";
       break;
       case "sub":
          result=
                  "@SP \n"+
                  "AM=M-1 \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "M=M-D";
       break;
       case "neg":
          result=
                  "@SP \n"+
                  "A=M-1 \n"+
                  "M=-M";
       break;
       case "gt":
          result="@SP\n" +
                  "AM = M - 1\n" +
                  "D = M\n" +
                  "A = A - 1\n" +
                  "MD = D-M\n" +
                  "@LABEL_TRUE"+n+"\n" +
                  "D;JLT\n" +
                  "@LABEL_FALSE"+n+"\n" +
                  "0;JMP\n" +
                  "(LABEL_TRUE"+n+")\n" +
                  "@SP\n" +
                  "A= M-1\n" +
                  "M = -1\n" +
                  "@OUT"+n+"\n" +
                  "0;JMP\n" +
                  "(LABEL_FALSE"+n+")\n" +
                  "@SP\n" +
                  "A= M-1\n" +
                  "M = 0\n" +
                  "(OUT"+n+")";
          n++;
        case "eq":
          result=
                  "@SP \n"+
                  "AM=M-1 \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "MD=D-M \n"+
                  "@LABEL_TRUE"+n+" \n"+ 
                  "D;JEQ \n"+ //if true, it jumps and does not execute the following code
                  "@SP \n"+ //if false, executes the following code.
                  "A=M-1 \n"+
                  "M=-1 \n"+
                  "(LABEL_TRUE"+n+")";
          n++;
       break;
       case "lt":
          result= "@SP\n" +
                  "AM = M - 1\n" +
                  "D = M\n" +
                  "A = A - 1\n" +
                  "MD = D-M\n" +
                  "@LABEL_TRUE"+n+"\n" +
                  "D;JLT\n" +
                  "@LABEL_FALSE"+n+"\n" +
                  "0;JMP\n" +
                  "(LABEL_TRUE"+n+")\n" +
                  "@SP\n" +
                  "A= M-1\n" +
                  "M = -1\n" +
                  "@OUT"+n+"\n" +
                  "0;JMP\n" +
                  "(LABEL_FALSE"+n+")\n" +
                  "@SP\n" +
                  "A= M-1\n" +
                  "M = 0\n" +
                  "(OUT"+n+")";
          n++;
       break;
       case "and":
          result = "@SP \n"+
                  "M=M-1 \n"+
                  "A=M \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "M=M&D";
       break;
       case "or":
          result = "@SP \n"+
                  "M=M-1 \n"+
                  "A=M \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "M=M|D";
       break;
       case "not":
          result = "@SP \n"+
                  "A=M-1 \n"+
                  "M=!M";
       break;
          default:
            result= "ARROR ARITHMETIC";
             break;
       }
       return result;
    }
    
    private String MemoryAccess(String[] parts)
    {
       String result="";
       int temp = 0;
       switch(parts[0]){
          case "push":
             switch(parts[1]){
                case "constant":
                   result= "@"+parts[2]+" \n"+
                           "D=A \n"+
                           pushStatements();
                   break;
                case "temp":
                   try{
                      int i = Integer.parseInt(parts[2]);
                      result = "@"+(i+5)+" \n"+
                               "D=M \n"+
                               pushStatements();
                   }
                   catch(Exception e){
                      result = "@5 \n"+
                              "D=A \n"+
                              "@"+parts[2]+" \n"+
                              "M=D+M \n"+
                              "D=M \n"+
                               pushStatements();
                   }
                   break;
                case "pointer":
                   String var = "THAT"; 
                   if(parts[2].equals("0")){ var = "THIS";}
                     result= "@"+var+" \n"+
                             "D=M \n"+
                             pushStatements();
                   break;
                case "this":
                   result = "@THIS \n"
                        + "D=M\n"
                        + "@" + parts[2] +"\n"
                        + "A=D+A\n"
                        + "D=M\n"                      
                        + pushStatements(); 
                   break;
                case "that": 
                   result = "@THAT \n"
                        + "D=M\n"
                        + "@" + parts[2] +"\n"
                        + "A=D+A\n"
                        + "D=M\n"                      
                        + pushStatements(); 
                           
                   break;
                case "argument": 
                   result = "@ARG \n"
                        + "D=M\n"
                        + "@" + parts[2] +"\n"
                        + "A=D+A\n"
                        + "D=M\n"                      
                        + pushStatements(); 
                           
                   break;
                case "local":
                   result = "@LCL \n"
                        + "D=M\n"
                        + "@" + parts[2] +"\n"
                        + "A=D+A\n"
                        + "D=M\n"                      
                        + pushStatements(); 
                   break;
                case "static":
                   try{
                      int b = Integer.parseInt(parts[2]);
                      result = "@" + (16 + b)+"\n"+
                           "D=M \n"+
                           pushStatements();

                   }
                   catch(Exception e)
                   {
                         result = "@" + parts[2]+"\n"+
                           "D=M \n"+
                           pushStatements();
                   }     
                   break;
                default:
                   result = "ERROR MEMORY ACCESS PUSH";
                   break;
             }
            break;
          case "pop":
             switch(parts[1]){
                case "temp":
                   result = "@SP \n" +
                            "AM=M-1 \n" +
                            "D=M \n" +
                            "@"+(5+Integer.parseInt(parts[2]))+"\n" +
                            "M = D";
//                   }
                   break;
                case "pointer":
                   String var = "THAT";
                   if(parts[2].equals("0")){var = "THIS";}
                   result = "@SP\n" +
                              "AM = M - 1 \n" +
                              "D = M \n" +
                              "@"+var+" \n" +
                              "A = M \n" +
                              "M = D";
                   break;
                case "that":
                   result = "@THAT \n"
                        + "D=M\n"
                        + "@"+parts[2]+"\n"
                        + "D=D+A\n"
                        + "@13\n"
                        + "M=D\n"
                        + "@SP\n"
                        + "AM=M-1\n"
                        + "D=M\n"
                        + "@13\n"
                        + "A=M\n"
                        + "M=D";   
            break;
                case "this":
                   result = "@THIS\n"
                        + "D=M\n"
                        + "@"+parts[2]+"\n"
                        + "D=D+A\n"
                        + "@13\n"
                        + "M=D\n"
                        + "@SP\n"
                        + "AM=M-1\n"
                        + "D=M\n"
                        + "@13\n"
                        + "A=M\n"
                        + "M=D";   
                   break;
                case "argument":
                   result = "@ARG \n"
                        + "D=M\n"
                        + "@"+parts[2]+"\n"
                        + "D=D+A\n"
                        + "@13\n"
                        + "M=D\n"
                        + "@SP\n"
                        + "AM=M-1\n"
                        + "D=M\n"
                        + "@13\n"
                        + "A=M\n"
                        + "M=D";   
                   break;
                case "local":
                   result = "@LCL \n"
                        + "D=M\n"
                        + "@"+parts[2]+"\n"
                        + "D=D+A\n"
                        + "@13\n"
                        + "M=D\n"
                        + "@SP\n"
                        + "AM=M-1\n"
                        + "D=M\n"
                        + "@13\n"
                        + "A=M\n"
                        + "M=D";   
                   break;
                case "static":
                   result="@SP \n" +
                           "AM=M-1 \n" +
                           "D=M \n" +
                           "@(16+n) \n" +
                           "M=D";
                   try{
                      int b = Integer.parseInt(parts[2]);
                      result="@SP \n" +
                           "AM=M-1 \n" +
                           "D=M \n" +
                           "@" + (16 + b)+"\n"+
                           "M=D";
                   }
                   catch(Exception e)
                   {
                      result="@SP \n" +
                           "AM=M-1 \n" +
                           "D=M \n" +
                           "@(16+"+parts[2]+") \n" +
                           "M=D";
                   }     
                   break;
                default:
                   result ="ERROR EN POP MEMORY ACCESS";
                   break;
             }
             break;
       }
       return result;
    }
    
    private String Flow(String[] parts)
    {
       String result="";
       
       switch(parts[0]){
          case "labels":
             result="//label \n"+
                     "("+parts[1]+")";
             break;
          case "if-goto":
             result = "//if-goto \n"
                     +"@SP \n"
                     + "AM=M-1 \n"
                     + "D=M \n"
                     + "@"+parts[1]+ " \n"
                     + "D;JNE";
             break;
          case "goto":
             result = "//goto \n"
                     +"@"+parts[1]+" \n"+
                     "0;JMP";
             break;
       }
       return  result;
    }
    
    private String Calls(String[] parts)
    {
       String result="";
       switch(parts[0]){
          case "function":
             int arguments = Integer.parseInt(parts[2]);
             for (int i = 0; i < arguments; i++) {
             result = "//function \n"
                     +"@0 \n"+
                        "D=A \n"+
                        pushStatements();
              }
             break;
          case "call":
             String returnLabel = parts[1] +""+(n++);
             result = "//call \n"+
                     "@" + returnLabel+ " \n"+
                        "D=A \n"+
                        pushStatements()+" \n"+
                        "//save LCL \n"+
                        "@LCL \n"+
                        "D=M \n"+
                        "//save arg \n"+
                        pushStatements()+" \n"+
                        "@ARG \n"+
                        "D=M \n"+
                     "//save this \n"+
                        pushStatements()+" \n"+
                        "@THIS \n"+
                        "D=M \n"+
                     "//save that \n"+
                        pushStatements()+" \n"+
                        "@THAT \n"+
                        "D=M \n"+
                     "//save pointer \n"+
                        pushStatements()+" \n"+
                        "@SP \n"+
                        "D=M \n"+
                        "@" + parts[2]+" \n"+
                        "D=D-A \n"+
                        "@5 \n"+
                        "D=D-A \n"+
                        "@ARG \n"+
                        "M=D \n"+
                     "//set the all new LCL pointer \n"+
                        "@SP \n"+
                        "D=M \n"+
                        "@LCL \n"+
                        "M=D \n"+
                        "@" + parts[1] + "\n"+
                        "0;JMP \n"+
                     "("+returnLabel+")";
             break;
          case "return":
            /* result = "// return\n" +
                  "@LCL\n" +
                  "D = M\n" +
                  "@R13\n" +
                  "M = D\n" +
                  "@5\n" +
                  "D = A\n" +
                  "@R13\n" +
                  "A = D\n" +//"A = M - D\n" +
                  "D = M\n" +
                  "@R14\n" +
                  "M = D\n" +
                  "// Save return value\n" +
                  "@SP\n" +
                  "A = M - 1\n" +
                  "D = M\n" +
                  "@ARG\n" +
                  "A = M\n" +
                  "M = D\n" +
                  "//4\n" +
                  "@ARG\n" +
                  "D = M + 1\n" +
                  "@SP\n" +
                  "M = D\n" +
                  "//5\n" +
                  "@1\n" +
                  "D = A\n" +
                  "@R13\n" +
                  "A =D\n" +//"A = M - D\n" +
                  "D = M\n" +
                  "@THAT\n" +
                  "M = D\n" +
                  "//6\n" +
                  "@2\n" +
                  "D = A\n" +
                  "@R13\n" +
                  "A =D\n" +//"A = M - D\n" +
                  "D = M\n" +
                  "@THIS\n" +
                  "M = D\n" +
                  "//7\n" +
                  "@3\n" +
                  "D = A\n" +
                  "@R13\n" +
                  "A = M - D\n" +//"A = M - D\n" +
                  "D = M\n" +
                  "@ARG\n" +
                  "M = D\n" +
                  "//8\n" +
                  "@4\n" +
                  "D = A\n" +
                  "@R13\n" +
                  "A = M - D\n" +
                  "D = M\n" +
                  "@LCL\n" +
                  "M = D\n" +
                  "//9\n" +
                  "@R14\n" +
                  "A = M\n" +
                  "0; JMP";*/
             
               result = "// return\n"
			+"// FRAME = LCL\n"
			+"@LCL\n"
			+"D=M\n"
			+"@R13\n"
			+"M=D\n"

			+"// RET = *(FRAME-5)\n"
			+"@R13\n"
			+"D=M\n"
			+"@5\n"
			+"A=D-A\n"
			+"D=M\n"
			+"@R14\n"
			+"M=D\n"

			+"// *ARG = pop()\n"
			+"@SP\n"
			+"M=M-1\n"
			+"A=M\n"
			+"D=M\n"
			+"@ARG\n"
			+"A=M\n"
			+"M=D\n"
			
			+"// SP = ARG + 1\n"
			+"@ARG\n"
			+"D=M\n"
			+"@1\n"
			+"D=D+A\n"
			+"@SP\n"
			+"M=D\n"

			+"// THAT = *(FRAME-1)\n"
			+"@R13\n"
			+"D=M\n"
			+"@1\n"
			+"A=D-A\n"
			+"D=M\n"
			+"@THAT\n"
			+"M=D\n"

			+"// THIS = *(FRAME-2)\n"
			+"@R13\n"
			+"D=M\n"
			+"@2\n"
			+"A=D-A\n"
			+"D=M\n"
			+"@THIS\n"
			+"M=D\n"

			+"// ARG = *(FRAME-3)\n"
			+"@R13\n"
			+"D=M\n"
			+"@3\n"
			+"A=D-A\n"
			+"D=M\n"
			+"@ARG\n"
			+"M=D\n"

			+"// LCL = *(FRAME-4)\n"
			+"@R13\n"
			+"D=M\n"
			+"@4\n"
			+"A=D-A\n"
			+"D=M\n"
			+"@LCL\n"
			+"M=D\n"

			+"// goto RET\n"
			+"@R14\n"
			+"A=M\n"
			+"0;JMP";
             break;
       }
       return result;
    }
    
    //repetitive statements
    private String pushStatements(){
       return "@SP \n"+ 
               "A=M \n"+ //guardo la direccion SP en A
               "M=D \n"+//meto el dato en m[a] (sp)
               "@SP \n"+
               "M=M+1"; //ahora sp se corre a la siguiente posición disponible
    }
    
    private String popStatements(){
       return saveDataPopNewAddress()+"\n@SP \n"+
               "A=M \n"+ //guardo la posición de SP en A
               "D=M \n"+ //D va a tomar el valor del dato en SP
               "@SP \n"+ 
               "M=M-1 \n" + //SP se corre una posición anterior
               savePopDataOtherStack(); //almacenamos el dato hecho pop en la posición que nos indicaron hacerlo (por eso la guardamos en el temporal)
    }
    
    private String saveDataPopNewAddress(){ //guardo la posición que me dicen que s para guardar el dato hecho pop
       return "D=A \n"+ //la posición en que se almacenara el dato hecho pop
               "@temp \n"+
               "M=D"; //lo guardamos en la temporal
    }
    
    private String savePopDataOtherStack(){
         return "@temp \n"+  //vamos a buscar el dato que hay en la temporal
                 "A=M \n"+ //El dato que hay en m, se vuelve una dirección
                 "M=D"; //almacenamos en esa dirección
    }
    
    private String addInit(){
         return "@256\n" +
                  "D = A\n" +
                  "@R0\n" +
                  "M = D\n" +
                  "@0\n" +
                  "D = A\n" +
                  "@R1\n" +
                  "M = D\n" +
                  "@0\n" +
                  "D = A\n" +
                  "@R2\n" +
                  "M = D\n" +
                  "@0\n" +
                  "D = A\n" +
                  "@R3\n" +
                  "M = D\n" +
                  "@0\n" +
                  "D = A\n" +
                  "@R4\n" +
                  "M = D";
      }
    
    private File[] OrderTheFiles(File[] files){
       boolean existMain=false;
       boolean existSys=false;
       
       for (int i = 0; i < files.length; i++)
       {
          File aux = null;
          if(files[i].getPath().contains("Main.vm")){
               aux = files[0];
               files[0] = files[i];
               files[i] = aux;
               existMain=true;
          }else if(files[i].getPath().contains("Sys.vm")){
             putInit = false;
              if(files.length>1){
              aux = files[1];
               files[1] = files[i];
               files[i] = aux;
               existSys=true;
              }
          }
       }
       
       if(!existMain && files.length>1){
          //for para buscar sys y ponerlo en la primera posición
          File aux = null;
          aux = files[0];
          files[0] = files[1];
          files[1] = aux;
       }
       return files;
    }
       
}
    

