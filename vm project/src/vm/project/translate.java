/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vm.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
   
   public translate(String path){
      try{
         ReadVmCode(path);
      }
      catch(Exception e){}
   }
   
   public String translateToHack(){
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
      return result;
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
                  "M=M+D";
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
          result=
                  "@SP \n"+
                  "AM=M-1 \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "D=D-M \n"+
                  "@EQUALSLABEL \n"+
                  "D;JGE \n";
//                  "@SP \n"+
//                  "A=M-1 \n"+
//                  "M=-1 \n"+
//                  "@ENDLABELS \n"+
//                  "0;JMP \n"+
//                  "(EQUALSLABEL) \n"+
//                  "@SP \n"+
//                  "A=M-1 \n"+
//                  "M=0 \n"+
//                  "(ENDLABELS)";
       break;
        case "eq":
          result=
                  "@SP \n"+
                  "AM=M-1 \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "D=D-M \n"+
                  "@EQUALSLABEL \n"+
                  "D;JNE \n";
//                  "@SP \n"+
//                  "A=M-1 \n"+
//                  "M=-1 \n"+
//                  "@ENDLABELS \n"+
//                  "0;JMP \n"+
//                  "(EQUALSLABEL) \n"+
//                  "@SP \n"+
//                  "A=M-1 \n"+
//                  "M=0 \n"+
//                  "(ENDLABELS)";
       break;
       case "lt":
          result= "@SP \n"+
                  "AM=M-1 \n"+
                  "D=M \n"+
                  "A=A-1 \n"+
                  "D=D-M \n"+
                  "@EQUALSLABELS \n"+
                  "D;JLE \n";
//                  "@SP \n"+
//                  "A=M-1 \n"+
//                  "M=-1 \n"+
//                  "@ENDLABELS \n"+
//                  "0;JMP \n" +
//                  "(EQUALSLABELS) \n"+
//                  "@SP \n"+
//                  "A=M-1 \n"+
//                  "M=0 \n "+
//                  "(ENDLABELS)";
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
       
       switch(parts[0]){
          case "push":
             switch(parts[1]){
                case "constant":
                   result= "@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@SP \n"+
                           "A=M \n"+
                           "M=D \n"+
                           "@SP \n"+
                           "M=M+1";
                   break;
                case "temp":
                   result = "@"+parts[2]+" \n"+
                           "D=M";
                   break;
                case "pointer":
                   result = "@"+parts[2] +" \n"+
                           "D=A \n"+
                           "@THIS \n"+
                           "A=A+D \n"+
                           "D=M";
                   break;
                case "this":
                   result = "@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@THIS \n"+
                           "A=M+D \n"+
                           "D=M";
                   break;
                case "that":
                   result = "@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@THAT \n"+
                           "A=M+D \n"+
                           "D=M";
                           
                   break;
                case "argument":
                   result="@ARG";
                   break;
                case "local":
                   result = "D=M \n"+
                           "@" + parts[2] +" \n"+
                           "A=D+A \n"+
                           "D=M";
                   break;
                case "static":
                   result = "@" + parts[2] + "." + parts[3] + "\nD=M\n";
                   break;
                default:
                   result = "ERROR MEMORY ACCESS";
                   break;
             }
            break;
          case "pop":
             switch(parts[1]){
                case "temp":
                   result="@"+(parts[2]+5);
                   break;
                case "pointer":
                   result= "@THIS \n";
                   int i = Integer.parseInt(parts[2]);
                   while(i-- > 0){
                      result+="A=A+1 \n";
                   }
                   result = result.substring(0, result.length()-2);
                   break;
                case "that":
                   result="@THAT";
                   break;
                case "this":
                   result="@THIS";
                   break;
                case "argument":
                   result="@ARG";
                   break;
                case "local":
                   result="@LCL \n"+
                           "D=M \n"+
                           "@"+parts[2]+"\n"+
                           "D=D+A \n"+
                           "M=D \n"+
                           "@SP \n"+
                           "AM=M-1 \n"+
                           "D=M \n"+
                           "@R15 \n"+
                           "A=M \n"+
                           "M=D";
                   break;
                case "static":
                   result= "@"+parts[2]+"."+parts[3];
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
}
