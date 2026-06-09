package com.estructuras;

public class Cola {
    private NodoCola frente;
    private NodoCola fin;
    private int tamaño;

    private class NodoCola {
        Cancion cancion;
        NodoCola siguiente;

        NodoCola(Cancion cancion) {
            this.cancion = cancion;
            this.siguiente = null;
        }
    }

    public Cola() {
        frente = fin = null;
        tamaño = 0;
    }

    public void encolar(Cancion cancion) {
        // Clone the Cancion to avoid modifying pointers in other lists/BST
        Cancion copia = new Cancion(cancion.nombre, cancion.artista, cancion.album, cancion.genero, cancion.duracionSeg, cancion.tamaño, cancion.ruta, cancion.año);
        NodoCola nuevo = new NodoCola(copia);
        if (estaVacia()) {
            frente = fin = nuevo;
        } else {
            fin.siguiente = nuevo;
            fin = nuevo;
        }
        tamaño++;
    }

    public Cancion desencolar() {
        if (estaVacia()) return null;
        Cancion cancion = frente.cancion;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamaño--;
        return cancion;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    public void limpiar() {
        frente = fin = null;
        tamaño = 0;
    }
}
