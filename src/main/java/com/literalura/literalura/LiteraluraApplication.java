package com.literalura.literalura;

import com.literalura.literalura.servicio.LibroService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class LiteraluraApplication implements CommandLineRunner {

	private final LibroService libroService;

	public LiteraluraApplication(LibroService libroService) {
		this.libroService = libroService;
	}

	public static void main(String[] args) {
		SpringApplication.run(LiteraluraApplication.class, args);
	}

	@Override
	public void run(String... args) {
		mostrarMenu();
	}

	private void mostrarMenu() {
		Scanner scanner = new Scanner(System.in);
		int opcion = -1;

		while (opcion != 0) {
			System.out.println("""
                =========================
                📚 LITERALURA - MENÚ
                =========================
					1 - Buscar libro por título
					2 - Mostrar libros guardados
					3 - Mostrar autores guardados
					4 - Mostrar libros por idioma
					5 - Mostrar autores vivos en un año
					0 - Salir
                =========================
                Ingrese una opción:
                """);

			try {
				opcion = Integer.parseInt(scanner.nextLine());

				switch (opcion) {
					case 1 -> {
						System.out.println("🔍 Ingrese el título del libro:");
						String titulo = scanner.nextLine();
						libroService.buscarLibroPorTitulo(titulo);
					}
					case 2 -> libroService.mostrarTodosLosLibros();
					case 3 -> {
						var libros = libroService.obtenerTodosLosLibros();
						if (libros.isEmpty()) {
							System.out.println("📘 No hay libros ni autores guardados.");
						} else {
							System.out.println("👤 Autores guardados:");
							libros.stream()
									.map(libro -> libro.getAutor().getNombre())
									.distinct()
									.forEach(nombre -> System.out.println("- " + nombre));
						}
					}
					case 4 -> {
						System.out.println("🌐 Ingrese el idioma (ej: en, es, fr):");
						String idioma = scanner.nextLine();
						libroService.mostrarLibrosPorIdioma(idioma);
					}
					case 5 -> {
						System.out.println("📅 Ingrese el año a consultar:");
						try {
							int anio = Integer.parseInt(scanner.nextLine());
							libroService.mostrarAutoresVivosEnAnio(anio);
						} catch (NumberFormatException e) {
							System.out.println("⚠️ Año no válido.");
						}
					}
					case 0 -> System.out.println("👋 ¡Hasta pronto!");
					default -> System.out.println("❌ Opción no válida. Intente de nuevo.");
				}
			} catch (NumberFormatException e) {
				System.out.println("⚠️ Entrada no válida. Ingrese un número.");
			}
		}

		scanner.close();
	}
}