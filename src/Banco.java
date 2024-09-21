import java.util.ArrayList;
import java.util.Scanner;

public class Banco {
    private ArrayList<Cliente> clientes = new ArrayList<>(); // Lista de clientes
    private Scanner scanner = new Scanner(System.in);

    // Constructor
    public Banco() {
        // Clientes iniciales de ejemplo
        clientes.add(new Cliente("Pedro", "Cuenta de Ahorro", 1000.0, "1234"));
        clientes.add(new Cliente("Luis", "Cuenta Corriente", 500.0, "5678"));
    }

    // Método para agregar un nuevo cliente
    public void agregarCliente(Cliente cliente) {
        clientes.add(cliente);
    }

    // Menú principal
    public void iniciarBanco() {
        int opcion;
        do {
            System.out.println("\nBienvenido a AluCash.");
            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrar nuevo cliente");
            System.out.println("3. Salir");
            System.out.print("Elige una opción: ");
            opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    iniciarSesion();
                    break;
                case 2:
                    registrarCliente();
                    break;
                case 3:
                    System.out.println("Gracias por usar AluCash.");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 3);
    }

    // Iniciar sesión
    private void iniciarSesion() {
        System.out.print("Ingresa tu nombre: ");
        String nombre = scanner.next();
        Cliente cliente = buscarCliente(nombre);

        if (cliente != null) {
            System.out.print("Ingresa tu contraseña: ");
            String contrasena = scanner.next();

            if (cliente.verificarContrasena(contrasena)) {
                System.out.println("Inicio de sesión exitoso. ¡Hola, " + cliente.getNombre() + "!");
                mostrarMenuCliente(cliente);
            } else {
                System.out.println("Contraseña incorrecta.");
            }
        } else {
            System.out.println("Cliente no encontrado.");
        }
    }

    // Registrar un nuevo cliente
    private void registrarCliente() {
        System.out.print("Ingresa tu nombre: ");
        String nombre = scanner.next();

        if (buscarCliente(nombre) == null) {
            System.out.print("Ingresa el tipo de cuenta (Ahorro/Corriente): ");
            String tipoCuenta = scanner.next();
            System.out.print("Ingresa el saldo inicial: ");
            double saldo = scanner.nextDouble();
            System.out.print("Crea una contraseña: ");
            String contrasena = scanner.next();

            Cliente nuevoCliente = new Cliente(nombre, tipoCuenta, saldo, contrasena);
            agregarCliente(nuevoCliente); // Usamos el método agregarCliente
            System.out.println("Cliente registrado con éxito.");
        } else {
            System.out.println("El nombre ya está en uso.");
        }
    }

    // Menú del cliente autenticado
    private void mostrarMenuCliente(Cliente cliente) {
        int opcion;
        do {
            System.out.println("\n¿Qué te gustaría hacer?");
            System.out.println("1. Consultar saldo");
            System.out.println("2. Depositar");
            System.out.println("3. Retirar");
            System.out.println("4. Transferir dinero");
            System.out.println("9. Cerrar sesión");
            System.out.print("Elige una opción: ");
            opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    // Consultar saldo
                    System.out.println("Saldo disponible: $" + cliente.getSaldo());
                    break;
                case 2:
                    // Depositar
                    System.out.print("Ingresa la cantidad a depositar: $");
                    double deposito = scanner.nextDouble();
                    cliente.depositar(deposito);
                    break;
                case 3:
                    // Retirar
                    System.out.print("Ingresa la cantidad a retirar: $");
                    double retiro = scanner.nextDouble();
                    cliente.retirar(retiro);
                    break;
                case 4:
                    // Transferir dinero
                    System.out.print("Ingresa el nombre del destinatario: ");
                    String nombreDestinatario = scanner.next();
                    Cliente destinatario = buscarCliente(nombreDestinatario);

                    if (destinatario != null) {
                        System.out.print("Ingresa la cantidad a transferir: $");
                        double cantidad = scanner.nextDouble();
                        cliente.transferir(destinatario, cantidad);
                    } else {
                        System.out.println("Destinatario no encontrado.");
                    }
                    break;
                case 9:
                    // Cerrar sesión
                    System.out.println("Cerrando sesión...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 9);
    }

    // Buscar un cliente por nombre
    public Cliente buscarCliente(String nombre) {
        for (Cliente cliente : clientes) {
            if (cliente.getNombre().equalsIgnoreCase(nombre)) {
                return cliente;
            }
        }
        return null;
    }
}