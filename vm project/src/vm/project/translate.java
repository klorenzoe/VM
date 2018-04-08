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
                           pushStatements();
                   break;
                case "temp":
                   int i = Integer.parseInt(parts[2]);
                   result = "@"+(i+5)+" \n"+
                           "D=M"+
                           pushStatements();
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
                           "D=M";
                   break;
                case "that":
                   result = "@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@THAT \n"+
                           "A=D+M \n"+
                           "D=M";
                           
                   break;
                case "argument":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@ARG \n"+
                           "A=D+M \n"+
                           "D=M";
                           
                   break;
                case "local":
                   result ="@"+parts[2]+" \n"+
                           "D=A \n"+
                           "@ARG \n"+
                           "A=D+M \n"+
                           "D=M";
                   break;
                case "static":
                   result = "@" + parts[2]+"\n"+
                           "D=M \n"+
                           pushStatements();
                   break;
                default:
                   result = "ERROR MEMORY ACCESS PUSH";
                   break;
             }
            break;
          case "pop":
             switch(parts[1]){
                case "temp":
                   result="@"+(parts[2]+5)+
                           popStatements();
                   break;
                case "pointer":
                   String var = "THAT";
                   if(parts[2].equals("0")){var = "THIS";}
                   result= "@"+var+" \n"+
                           popStatements();
                   break;
                case "that":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@THAT \n"+
                           "A=D+M \n"+ //sumamos el dato con lo que posee that y lo guardamos en A como direccion
                           popStatements();
                   break;
                case "this":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@THIS \n"+
                           "A=D+M \n"+ //sumamos el dato con lo que posee that y lo guardamos en A
                           popStatements();
                   break;
                case "argument":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@ARG \n"+
                           "A=D+M \n"+ //sumamos el dato con lo que posee ARG y lo guardamos en A
                           popStatements();
                   break;
                case "local":
                   result="@"+parts[2]+" \n"+
                           "D=A \n"+  //guardamos el dato en D
                           "@LCL \n"+
                           "A=D+M \n"+ //sumamos el dato con lo que posee LCL y lo guardamos en A
                           popStatements();
                   break;
                case "static":
                   result= "@"+parts[2]+" \n"+ //obtenemos la nueva posición al que ira el dato pop
                           popStatements(); //le hacemos pop
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
}
