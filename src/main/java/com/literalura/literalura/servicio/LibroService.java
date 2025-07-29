package com.literalura.literalura.servicio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.literalura.literalura.modelo.Libro;
import com.literalura.literalura.modelo.Autor;
import com.literalura.literalura.repositorio.LibroRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public void cargarLibrosDesdeApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gutendex.com/books/"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            JsonNode resultados = root.get("results");

            List<Libro> libros = new ArrayList<>();

            for (JsonNode nodo : resultados) {
                Libro libro = new Libro();
                libro.setTitulo(nodo.get("title").asText());
                JsonNode autorJson = nodo.get("authors").get(0);
                Autor autor = new Autor();
                autor.setNombre(autorJson.get("name").asText());
                autor.setAnioNacimiento(autorJson.get("birth_year").asInt());
                autor.setAnioFallecimiento(autorJson.get("death_year").asInt());
                libro.setAutor(autor);
                libro.setIdioma(nodo.get("languages").get(0).asText());
                libro.setNumeroDeDescargas(nodo.get("download_count").asInt());
                libros.add(libro);
            }

            libroRepository.saveAll(libros);
            System.out.println("Libros guardados exitosamente en la base de datos.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void buscarLibroPorTitulo(String titulo) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://gutendex.com/books/?search=" + titulo.replace(" ", "+")))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            JsonNode resultados = root.get("results");

            if (resultados.isEmpty()) {
                System.out.println("No se encontraron libros con ese título.");
                return;
            }

            JsonNode libroJson = resultados.get(0);
            Libro libro = new Libro();
            libro.setTitulo(libroJson.get("title").asText());
            JsonNode autorJson = libroJson.get("authors").get(0);
            Autor autor = new Autor();
            autor.setNombre(autorJson.get("name").asText());
            autor.setAnioNacimiento(autorJson.get("birth_year").asInt());
            autor.setAnioFallecimiento(autorJson.get("death_year").asInt());
            libro.setAutor(autor);
            libro.setIdioma(libroJson.get("languages").get(0).asText());
            libro.setNumeroDeDescargas(libroJson.get("download_count").asInt());

            libroRepository.save(libro);

            System.out.println("📚 Libro guardado exitosamente:");
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Descargas: " + libro.getNumeroDeDescargas());

        } catch (Exception e) {
            System.out.println("❌ Error al buscar el libro: " + e.getMessage());
        }
    }

    public void mostrarTodosLosLibros() {
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("📕 No hay libros guardados en la base de datos.");
            return;
        }

        System.out.println("\n📚 Listado de todos los libros:");
        for (Libro libro : libros) {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Descargas: " + libro.getNumeroDeDescargas());
            System.out.println("--------------------------");
        }
    }

    public void mostrarLibrosPorIdioma(String idioma) {
        List<Libro> libros = libroRepository.findByIdioma(idioma);
        if (libros.isEmpty()) {
            System.out.println("📘 No se encontraron libros en el idioma: " + idioma);
            return;
        }

        System.out.println("\n📗 Libros en idioma '" + idioma + "':");
        for (Libro libro : libros) {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Descargas: " + libro.getNumeroDeDescargas());
            System.out.println("--------------------------");
        }

        System.out.println("📊 Total de libros en idioma '" + idioma + "': " + libros.size());
    }

    public void mostrarAutoresVivosEnAnio(int anioConsulta) {
        List<Libro> autoresVivos = new ArrayList<>();

        // Autores con año de fallecimiento conocido
        autoresVivos.addAll(libroRepository
                .findByAutorAnioNacimientoLessThanEqualAndAutorAnioFallecimientoGreaterThan(anioConsulta, anioConsulta));

        // Autores sin año de fallecimiento (se asume vivos aún)
        autoresVivos.addAll(libroRepository
                .findByAutorAnioNacimientoLessThanEqualAndAutorAnioFallecimientoIsNull(anioConsulta));

        if (autoresVivos.isEmpty()) {
            System.out.println("👤 No se encontraron autores vivos en el año " + anioConsulta);
            return;
        }

        System.out.println("\n👤 Autores vivos en el año " + anioConsulta + ":");
        autoresVivos.stream()
                .map(libro -> libro.getAutor().getNombre())
                .distinct()
                .forEach(nombre -> System.out.println("- " + nombre));
    }

    public void mostrarLibrosPorAutor(String nombreAutor) {
        List<Libro> libros = libroRepository.findByAutorNombreContainingIgnoreCase(nombreAutor);
        if (libros.isEmpty()) {
            System.out.println("📘 No se encontraron libros del autor: " + nombreAutor);
            return;
        }

        System.out.println("\n📗 Libros de '" + nombreAutor + "':");
        for (Libro libro : libros) {
            System.out.println("Título: " + libro.getTitulo());
            System.out.println("Autor: " + libro.getAutor());
            System.out.println("Idioma: " + libro.getIdioma());
            System.out.println("Descargas: " + libro.getNumeroDeDescargas());
            System.out.println("--------------------------");
        }
    }

    public List<Libro> obtenerTodosLosLibros() {
        return libroRepository.findAll();
    }
}