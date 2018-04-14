/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vm.project;

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
   
   public translate(String path){
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
            case "add": case "sub": case "neg": case "gt": case "lt": case "and": case "or": case "not":
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
      return addInit()+ result;
   }
    private void ReadVmCode(String fileName)throws  Exception{
      
       File entry = new File(fileName);
       BufferedReader codeFile = new BufferedReader(new FileReader(entry));
       
       String line;
         while ((line = codeFile.readLine()) != null){
            if(line.contains("//")){
               String noComents = line.split("//")[0];
               if(!noComents.isEmpty()){
                  vmCode.add(line.split("//")[0].trim());
                  vmCodeContent+=line.split("//")[0].trim()+"\n";
               }
            }else{
               if(!line.isEmpty()){
                  vmCode.add(line.trim());
               }
            }
            vmCodeContent+=line.trim()+"\n";
         }
       codeFile.close();
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
                               "D=M"+
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
               /*    result = "@"+parts[2] +" \n"+
                           "D=A \n"+
                           "@"+var+" \n"+
                           "A=A+D \n"+
                           "D=M";*/
                     result= "@"+var+" \n"+
                             "D=M \n"+
                             pushStatements();
                   break;
                case "this":
                   result = "@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@THIS \n"+
                           "A=D+M \n"+
                           "D=M \n"+
                           pushStatements();
                   break;
                case "that": 
                   result = "@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@THAT \n"+
                           "A=D+M \n"+
                           "D=M \n"+
                           pushStatements();
                           
                   break;
                case "argument": 
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@ARG \n"+
                           "A=D+M \n"+
                           "D=M \n"+
                           pushStatements();
                           
                   break;
                case "local":
                   try{
                   int a = Integer.parseInt(parts[2]);
                   result ="@LCL \n"+
                           "D=M \n"+
                           "@"+a+"\n"+
                           "A=D+A \n"+
                           "D=M \n";
                   }catch(Exception e){
                   result ="@LCL \n"+
                           "D=M \n"+
                           "@"+parts[2]+" \n"+
                           "A=M+D \n"+
                           "D=M";}
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
                            "@("+(5+Integer.parseInt(parts[2]))+")\n" +
                            "M = D";
//                   }
                   break;
                case "pointer":
                   String var = "THIS";
                   if(parts[2].equals("0")){var = "THAT";}
                   result = "@SP\n" +
                              "AM = M - 1 \n" +
                              "D = M \n" +
                              "@"+var+" \n" +
                              "A = M \n" +
                              "M = D";
                   break;
                case "that":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@THAT \n"+
                           "D=D+M \n"+
                           "@13 \n"+
                           "M=D \n"+
                           "@SP \n"+
                           "AM=M-1 \n"+
                           "D=M \n"+
                           "@13 \n"+
                           "A=M \n"+
                           "M=D \n";
            break;
                case "this":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@THIS \n"+
                           "D=D+M \n"+
                           "@13 \n"+
                           "M=D \n"+
                           "@SP \n"+
                           "AM=M-1 \n"+
                           "D=M \n"+
                           "@13 \n"+
                           "A=M \n"+
                           "M=D \n";
                   break;
                case "argument":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@ARG \n"+
                           "D=D+M \n"+
                           "@13 \n"+
                           "M=D \n"+
                           "@SP \n"+
                           "AM=M-1 \n"+
                           "D=M \n"+
                           "@13 \n"+
                           "A=M \n"+
                           "M=D \n";
                   break;
                case "local":
                   try{
                     temp = Integer.parseInt(parts[2]);
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@LCL \n"+
                           "A=D+M \n"+ //sumamos el dato con lo que posee LCL y lo guardamos en A
                           popStatements();
                   }catch(Exception e){
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@LCL \n"+
                           "A=D+M \n"+ //sumamos el dato con lo que posee LCL y lo guardamos en A
                           popStatements();
                   }
                   break;
                case "static":
                   result="@SP \n" +
                           "AM=M-1 \n" +
                           "D=M \n" +
                           "@(16+n) \n" +
                           "M=D";
//                   }  
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
             break;
          case "if-goto":
             
             break;
          case "goto":
             break;
       }
       return  result;
    }
    
    private String Calls(String[] parts)
    {
       String result="";
       switch(parts[0]){
          case "function":
             break;
          case "call":
             break;
          case "return":
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
                  "M = D\n";
      }
}
