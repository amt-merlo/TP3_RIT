/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tp3rit;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


//para parsear el sgml
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.*;
import org.xml.sax.InputSource;
import org.w3c.dom.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import java.lang.Math; //para el log
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner; //input

/**
 *
 * @author Allison
 */
public class main {
    public static int cantDocu;
    public static List articulos = new ArrayList(); 
    public static Map<String, String> clasesText;
    
    
    public static List sacarTXT(String path){ //saca las lineas del txt y las guarda en una lista
    File archivo = null;
    FileReader fr = null;
    BufferedReader br = null;
    List lista = new ArrayList();
    String finale="";
    try {
         // Apertura del fichero y creacion de BufferedReader para poder
         // hacer una lectura comoda (disponer del metodo readLine()).
         archivo = new File (path);
         fr = new FileReader (archivo);
         br = new BufferedReader(fr);

         // Lectura del fichero
         String linea;
         while((linea=br.readLine())!=null)
            //System.out.println(linea);
            //finale = linea.replaceAll("[^a-z]", "");
            //System.out.println(finale);
            
            lista.add(linea);
      }
      catch(Exception e){
         e.printStackTrace();
      }finally{
         // En el finally cerramos el fichero, para asegurarnos
         // que se cierra tanto si todo va bien como si salta 
         // una excepcion.
         try{                    
            if( null != fr ){   
               fr.close();     
            }                  
         }catch (Exception e2){ 
            e2.printStackTrace();
         }
      }
    
    return lista; //devuelve un arraylist con las lineas del txt
    } 
    
    
    public static List limpiarArticulos(List lineas){
        List listafinal = new ArrayList();
        String actual;
        
        for(Object linea:lineas){
            actual=linea.toString();
            actual = actual.replaceAll("&#", "");
            actual = actual.replaceAll("#$", "");
            actual = actual.replaceAll(",", "");
            
            listafinal.add(actual);
        }
        
        return listafinal;
    }
    
    public static List dividirArticulos(List cadena){  //toma la lista con las lineas del txt y devuelve una lista con las páginas html
        String linea="", pagina="";
        
        int contador = 0, bandera = 0; //contador para saber cantidad de paginas y bandera para saber cuándo agregar la linea al string de la página
        List paginas = new ArrayList(); //arraylist final con el txt separado por páginas
        
        
        //para iterar la lista con las líneas del txt
        Iterator it = cadena.iterator();
        
        while(it.hasNext()){
            linea=it.next().toString(); //combierte el object del arraylist en string 
            
            //buscar la palabra Reuters
            int indiceInicio = linea.indexOf("<REUTERS");
            int indiceFinal = linea.indexOf("</REUTERS>");
            
            if (indiceInicio!=-1){ //si el indice es diferente de -1 entonces encontró la cadena <!DOCTYPE html donde la nueva página inicia
                
                contador=contador+1; //para saber cuántas páginas hay
                bandera=1; //para que agrege la linea al String de la página
                
            }else if(indiceFinal!=-1){ //si el indice es diferente de -1 entonces encontró el final de la página
                
                pagina = pagina+linea+"\n"; //agrega la última linea
                bandera=0; //para que agregue la página a la lista y se reinicie
            }
      
            //si la bandera está en 1 agrega la línea al String de la página, si está en 0 agrega el String a la lista y lo vacía 
            if (bandera == 1){
                pagina = pagina+linea+"\n";
            }else{
                paginas.add(pagina);
                pagina = "";
            }
            
        }
        System.out.println("Cantidad de páginas procesadas: "+ contador);
        cantDocu = contador;
        
        return paginas;
    }
    
    public static String removerStopWords(String texto){
        try{
            String archivo = "C:\\Users\\gabyg\\Documents\\GitHub\\TP3_RIT\\stopwords.txt"; //  C:\\Users\\gabyg\\Documents\\GitHub\\TP3_RIT\\stopwords.txt  C:\\Users\\Allison\\Desktop\\TP3\\stopwords.txt
            List<String> stopwords = Files.readAllLines(Paths.get(archivo));
            String[] nuevo = texto.toLowerCase().split(" ");
            StringBuilder builder = new StringBuilder();
            for (String palabra: nuevo){
                    if(!stopwords.contains(palabra)) {
                        builder.append(palabra);
                        builder.append(' ');
                    }
            }
            String result = builder.toString().trim();
            
            return result;
        }
        catch (Exception e){
            System.out.println("No se pudo abrir el archivo de stopwords");
            return null;
        }
    }
    
    
    public static Document makeParser(String pagina){ //crea un objeto de tipo document para poder parsear el sgml
        
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setEncoding("UTF-8");
            is.setCharacterStream(new StringReader(pagina));
            Document doc = db.parse(is);
            
            return doc;
            
        }
        catch (Exception e) {
            System.out.println("Problemas con el parser");
            e.printStackTrace();
           
        }
        return null; //este array se usa para indexar
    }
    

    public static String getCharacterDataFromElement(Element e) { 
    Node child = e.getFirstChild();
    if (child instanceof CharacterData) {
       CharacterData cd = (CharacterData) child;
       return cd.getData();
    }
    return "?";
  }
    
    public static Map clases(Map clases, List articulos){ //crea un diccionario con las clases y la cantidad de articulos que son de esa clase
        List topics = new ArrayList(); //guardar las clases que hay
        String topic;
        int cantidad;
        
        
        for (Object articulo:articulos){ //para cada pagina el mismo proceso
           
            //obtiene el doc para parsear 
            Document doc = makeParser(articulo.toString());
            
            //Saca el texto de la etiqueta topic
            NodeList nodes = doc.getElementsByTagName("TOPICS");
            Node nNode = nodes.item(0);
            topic = nNode.getTextContent(); 
            
            if (topic.length()>0){ //lo agrega a la lista solo si no está en blanco
                topics.add(topic);
            }
        }
        
        for (Object tema:topics){ //recorre lista de temas obtenidos
            if(clases.containsKey(tema)){ //pregunta si ya está en el diccionario
                cantidad = (int)clases.get(tema); 
                clases.put(tema, cantidad+1); //aumenta la cantidad de articulos con ese tema
            }else{
                clases.put(tema, 1); //si no está, loo inicializa en uno
            }
        }
       
        return clases;
    }
    
    public static void crearClases(int minNc, Map clases, String prefijo) throws IOException{ //crea el txt con las clases  
        prefijo = prefijo+"_clases.txt";
        String cadena=""; //para agregar las clases y cant de articulos
        
        Iterator it = clases.entrySet().iterator();
        while (it.hasNext()) { //recorre el diccionario de clases
            Map.Entry e = (Map.Entry)it.next(); 
            int value = (int) e.getValue(); //cantidad de articulos
            String key = (String) e.getKey(); //clase
            
            if(value>=minNc){ //si la cantidad de articulos es igual o mayor que el minimo indicado por el usuario
                cadena = cadena + key + "\t" + value + "\n"; //lo agrega a la cadena
            }
            
        }
        
        //=================CREAR EL DOC===================
        String ruta = "C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\"; //  C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\  C:\\Users\\Allison\\Desktop\\TP3\\
        String rutaFinal = ruta+prefijo;
        
        File file = new File(rutaFinal);
        
        // Si el archivo no existe es creado
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(cadena);
        bw.close();
    }
    
    public static List crearDocs(Map clases, List articulos, int minNc, String prefijo) throws IOException{ //crea el txt docs
        String topic;
        String NEWID;
        String topYN;
        String cadena = ""; //para escribir el archivo
        int cantidad;
        List docs = new ArrayList();
        prefijo = prefijo+"_docs.txt";
        
        for (Object articulo:articulos){ //para cada pagina el mismo proceso
           
            //obtiene el doc para parsear 
            Document doc = makeParser(articulo.toString());
            
            //Saca el texto de la etiqueta topic
            NodeList nodes = doc.getElementsByTagName("TOPICS");
            Node nNode = nodes.item(0);
            topic = nNode.getTextContent();  //sacamos el tema
            
            //saca el codigo de la etiqueta NEWID
            NodeList nodes2 = doc.getElementsByTagName("REUTERS");
            NEWID = nodes2.item(0).getAttributes().getNamedItem("NEWID").getNodeValue(); //saca el NEWID
            
           //saca la condicion YES/NO de la etiqueta TOPICS
           NodeList nodes3 = doc.getElementsByTagName("REUTERS");
           topYN = nodes3.item(0).getAttributes().getNamedItem("TOPICS").getNodeValue(); //saca la condicion
           
           
           if (clases.containsKey(topic)){  
               cantidad = (int)clases.get(topic); 
               if (cantidad>=minNc){ //verifica que el doc es parte de las clases del txt
                   if(topYN.equals("YES")){ //verifica que el atributo TOPICS esté en YES
                       cadena = cadena + NEWID + "\t" + topic + "\n";
                       docs.add(articulo);
                   }
               }
           }
        }
        
        //=================CREAR EL DOC===================
        String ruta = "C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\"; // C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\ C:\\Users\\Allison\\Desktop\\TP3\\
        String rutaFinal = ruta+prefijo;
        
        File file = new File(rutaFinal);
        // Si el archivo no existe es creado
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(cadena);
        bw.close();
        return docs;
    }
    
    public static Map<String, Integer> crearDicc(List docs, String prefijo) throws IOException{ //crea el txt dicc
        String body="", acumTexto = "";
        Map<String, Integer> diccionario = new HashMap<String, Integer>(); //diccionario de clases
        int cantidad;
        prefijo = prefijo+"_dicc.txt";
        
        for (Object articulo:docs){
            List palabras = new ArrayList(); //lista de palabras de todos los articulos
            //obtiene el doc para parsear 
            Document doc = makeParser(articulo.toString());
            
            //Saca el texto de la etiqueta topic
            NodeList nodes = doc.getElementsByTagName("BODY");
            Node nNode = nodes.item(0);
            if (nNode!=null){ //a veces está vacío y se cae xd por eso el if
                body = nNode.getTextContent();  //sacamos el texto de body
            }
            
            //quita stopwords
            body = removerStopWords(body.toLowerCase());
            //quita saltos de linea
            body = body.replaceAll("\n", "");
            body = body.replaceAll("", "");
            //separa las palabras
            String[] nuevo = body.split(" ");
            
            
            //agregar las palabras del articulo a una lista 
            for (String palabra: nuevo){
                if (palabra.length()>0){
                    if(!palabras.contains(palabra)){
                        palabras.add(palabra);
                    }
                }
            }
            
            //Compara las palabras del diccionario con las de la lista, si la palabra ya estaba entonces aumenta el contador en 1 para indicar que la palabra estaba en ese documento
            for (Object palabra:palabras){
                if(diccionario.containsKey(palabra)){
                    cantidad = (int)diccionario.get(palabra); 
                    diccionario.put(palabra.toString(), cantidad+1);
                }
                else{
                    diccionario.put(palabra.toString(), 1);
                }
            }
        }
        
        
        //construir el string para escribir en el documento dicc.txt
        String cadena=""; //para agregar las clases y cant de articulos
        
        Iterator it = diccionario.entrySet().iterator();
        while (it.hasNext()) { //recorre el diccionario de clases
            Map.Entry e = (Map.Entry)it.next(); 
            int value = (int) e.getValue(); //cantidad de articulos
            String key = (String) e.getKey(); //clase
            
            cadena = cadena + key + "\t" + value + "\n";
            
        }
        
        
        //=================CREAR EL DOC===================
        String ruta = "C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\"; //  C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\  C:\\Users\\Allison\\Desktop\\TP3\\
        String rutaFinal = ruta+prefijo;
        
        File file = new File(rutaFinal);
        // Si el archivo no existe es creado
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(cadena);
        bw.close();
        return diccionario;
    }
    
    //Elimina todos los términos que tengan menos del minNi
    public static Map<String, Integer> descartarTerminos(Map<String, Integer> dicci, int min){
        Map<String, Integer> nuevo = new HashMap<String, Integer>();
        Iterator it = dicci.entrySet().iterator();
        while (it.hasNext()) { //recorre el diccionario de clases
            Map.Entry e = (Map.Entry)it.next(); 
            int value = (int) e.getValue(); //cantidad de articulos
            String key = (String) e.getKey(); //clase
            if(value >= min){ //si esta mas veces que el minNi
                nuevo.put(key, value); //agrega al nuevo diccionario
            }
        }
        return nuevo;
    }
    
    //calcula E(C)
    public static double calcularEntropiaColeccion(Map<String, Integer> clases){
        
        float suma = 0, paginas=0;
        float resultado;
        float log = 0;
        
        for (Map.Entry<String, Integer> entry : clases.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            resultado = (float) value / cantDocu;
            log = (float) (Math.log10(resultado) / Math.log10(2));
            resultado = resultado * log;
            suma = suma + resultado;
            paginas = paginas+value;
        }
        
        suma = suma * -1;
        return suma;
        
    }
    
    //saca clase y texto de todas las de ese tipo concatenado
    public static Map dividirClases(List articulos){
        Map<String, String> clases = new HashMap<String, String>();
        String texto = "", nuevo = "", body="";
        
        for (Object articulo: articulos){
            Document doc = makeParser(articulo.toString());
            
            //saca el tema
            NodeList node1 = doc.getElementsByTagName("TOPICS");
            Node nNode1 = node1.item(0);
            String topic = nNode1.getTextContent(); 
            
             //saca el texto
            NodeList node = doc.getElementsByTagName("BODY");
            Node nNode = node.item(0);
            if (nNode!=null){ //a veces está vacío y se cae xd por eso el if
                 body = nNode.getTextContent();  //sacamos el texto de body
                 //quita stopwords
                 body = removerStopWords(body.toLowerCase());
                 //quita saltos de linea
                 body = body.replaceAll("\n", "");
                 body = body.replaceAll("", "");
                

            }
            
            if (topic.length()>0){
                if (clases.containsKey(topic)){
                    texto = clases.get(topic);
                    texto = texto + "///"+body;
                    clases.put(topic, texto);
                }
                else{
                    clases.put(topic, body);
                }
            }
        }
       
        return clases;
    }
    
    //Calcula la cantidad de articulos de la clase k con el término i
    public static int calcularNik(String termino, String tema){
        String body="";
        int total=0;
        
        
        body = clasesText.get(tema);
        
        String[] dividido = body.split("///");
        for (String pagina : dividido){
            
            if (pagina.contains(termino.toString())){
                total = total+1;
            }
        }
        
            
        /*
        for (Object articulo : articulos){
            Document doc = makeParser(articulo.toString());
            
            //saca el tema
            NodeList node1 = doc.getElementsByTagName("TOPICS");
            Node nNode1 = node1.item(0);
            String topic = nNode1.getTextContent(); 
            if (topic.length()>0){ //si hay un tema
                
                if (topic.equals(tema)){ //si el tema es igual al que buscamos
                    //Saca el texto de la etiqueta topic
                    NodeList nodes = doc.getElementsByTagName("BODY");
                    Node nNode = nodes.item(0);
                    if (nNode!=null){ //a veces está vacío y se cae xd por eso el if
                        body = nNode.getTextContent();  //sacamos el texto de body
                    }
            
                    //quita stopwords
                    body = removerStopWords(body.toLowerCase());
                    //quita saltos de linea
                    body = body.replaceAll("\n", "");
                    body = body.replaceAll("", "");
                    String[] palabras = body.split(" ");

                    for (String palabra : palabras){
                        if (palabra.equals(termino)){
                            total = total + 1;
                        }
                    }
            
                    
                }
            }
            
        }*/
        
        return total;
    }
    
    
    //Calcula el E(C,〖term〗_i )  
    public static String calcularGananciaInformacion(Map<String, Integer> clases, Map<String, Integer> dicci){
        
        double entropiaC = calcularEntropiaColeccion(clases); //se calcula E(C) la entropía de toda la colección
        String cadena="";
        System.out.println("Entropía de las clases: "+ entropiaC);
        
        
        //Para ordenar el HashMap
        List<String> ordenado = new ArrayList<>(dicci.keySet());
        
        Collections.sort(ordenado);
        
        //para cada termino
        for (Object termino : ordenado){
           double total;
           double sumatoria1 = 0.0, sumatoria2 = 0.0;
           
           
           for (Map.Entry<String, Integer> clase : clases.entrySet()){ //por cada clase
               double resultado=0.0, resultado2 = 0.0;
               
               
               //obtener primera sumatoria
               int nik = calcularNik(termino.toString(), clase.getKey()); //cantidad de articulos con el termino i de la clase k
               
               if (nik>0){ //si esto no se hace, el logaritmo da NaN
                   resultado = nik * (Math.log10(nik) / Math.log10(2)); //esto es (nik*log2(nik)
               }
               
               
               sumatoria1 = sumatoria1 + resultado;
               
               
               //calcula segunda sumatoria
               int nck = clase.getValue(); //cantidad de articulos de la clase k
               if (nck-nik>0){ //si esto no se hace, el logaritmo da NaN
                   resultado2 = (nck - nik)*(Math.log10(nck - nik) / Math.log10(2));
               }
               
               sumatoria2 = sumatoria2 + resultado2;
            } 
           
           int ni = dicci.get(termino);
           double parte1, parte2;
           
           parte1 = ni * (Math.log10(ni) / Math.log10(2));
           parte2 = (cantDocu-ni)*(Math.log10(cantDocu-ni) / Math.log10(2));
           
           
           total = parte1 + parte2 - sumatoria1 - sumatoria2;
           total = total / cantDocu;
           
           double GI = entropiaC - total;
           
           //forma el string que se escribirá en el doc
           cadena = cadena + termino + "\t" + total + "\t" + GI + "\n";
           
           //para agregar al diccionario de mejores
           //Para ordenar el HashMap
           
        }
        
       
        System.out.println(cadena);
        
        return cadena;
        
    }
    
    public static void crearGI(String cadena, String prefijo) throws IOException{
        prefijo = prefijo + "_gi.txt";
        //=================CREAR EL DOC===================
        String ruta = "C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\"; //  C:\\Users\\gabyg\\Downloads\\Tarea_programada_3\\  C:\\Users\\Allison\\Desktop\\TP3\\
        String rutaFinal = ruta+prefijo;
        
        File file = new File(rutaFinal);
        // Si el archivo no existe es creado
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(cadena);
        bw.close();
        
    }
   
    public static void main(String[] args) throws IOException {
        //Variables a usar 
        List lineas = new ArrayList(); 
        List lineasLimpias = new ArrayList(); 
        List docs = new ArrayList(); //guarda los articulos seleccionados en la clase docs
        Map<String, Integer> clases = new HashMap<String, Integer>(); //diccionario de clases con cant de docs por clase
        Map<String, Integer> diccionario = new HashMap<String, Integer>(); //diccionario de términos 
        
        
        //Pide los parámetros
        Scanner myObj = new Scanner(System.in);
        System.out.println("Ingrese la ruta:");
        String path;
        path = myObj.nextLine(); 
        System.out.println("Ingrese el prefijo:");
        String prefijo; // = "pr1";
        prefijo = myObj.nextLine();
        System.out.println("Ingrese la cantidad de mejores términos:");
        int numMejores = Integer.parseInt(myObj.nextLine()); 
        System.out.println("Ingrese la cantidad mínima de documentos por clase:");
        int minNc = Integer.parseInt(myObj.nextLine()); //8
        System.out.println("Ingrese la cantidad mínima de documentos por término:");
        int minNi = Integer.parseInt(myObj.nextLine()); //3
        
        
        
         
        
        
        
        //Procesamiento del SGML
        lineas = sacarTXT(path); //saca las lineas del sgml en una lista
        lineasLimpias = limpiarArticulos(lineas); //quita caracteres especiales
        articulos = dividirArticulos(lineasLimpias); //devuelve una lista con los articulos separados
        articulos.remove(0); 
      
       //construir diccionario con las clases
       clases = clases(clases, articulos);
       
       //crear clases.txt
       crearClases(minNc, clases, prefijo);
       //crear docs.txt
       docs = crearDocs(clases, articulos, minNc, prefijo);
       //crear dicc.txt
       diccionario = crearDicc(docs, prefijo);
       //descartar terminos con minNi
       diccionario = descartarTerminos(diccionario, minNi);
       //saca clase y texto
       clasesText = dividirClases(articulos);
        
       String GI = calcularGananciaInformacion(clases, diccionario);
       //Crea gi.txt
       crearGI(GI, prefijo);
    }
    
}
