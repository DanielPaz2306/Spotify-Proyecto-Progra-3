package com.estructuras;

public class ListaDobleEnlazadaCircular {

    public Cancion inicio;
    public Cancion fin;
    public int contador;

    private ListaDobleEnlazadaCircular() {
        inicio = fin = null;
        contador = 0;
    }

    public boolean EstaVacia() {
        return inicio == null;
    }

    public void Insertar(String nombre, String artista, String album, String genero, int duracion, long tamaño, String ruta, String año){
        Cancion nuevo = new Cancion(nombre, artista, album, genero, duracion, tamaño, ruta, año, null, null);
        
        if(EstaVacia()){
            inicio = fin = nuevo;
            
            inicio.siguiente = inicio;
            inicio.anterior = inicio;
        }
        
        else{
            nuevo.siguiente = inicio;
            nuevo.anterior = fin;
            inicio.anterior = nuevo;
            fin.siguiente = nuevo;
            inicio = nuevo;
        }
        
        contador ++;
    }
    
    public Cancion siguiente(Cancion actual){
        return actual.siguiente;
    }
    
    public Cancion anterior(Cancion actual) {
        return actual.anterior;
    }
    
    public void eliminar(Cancion c) {
        if (EstaVacia()) return;

        c.anterior.siguiente = c.siguiente;
        c.siguiente.anterior = c.anterior;

        if (c == inicio) inicio = c.siguiente;
        if (c == fin) fin = c.anterior;
        if (inicio == c) inicio = fin = null; // era el único

        contador--;
    }
}
