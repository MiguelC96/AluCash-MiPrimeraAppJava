import java.util.ArrayList;
import java.util.Scanner;

class Cliente {
    private String nombre;
    private String tipoCuenta;
    private double saldo;
    private String contrasena;

    // Constructor
    public Cliente(String nombre, String tipoCuenta, double saldo, String contrasena) {
        this.nombre = nombre;
        this.tipoCuenta = tipoCuenta;
        this.saldo = saldo;
        this.contrasena = contrasena;
    }

    // Getter para nombre
    public String getNombre() {
        return nombre;
    }

    // Getter para tipo de cuenta
    public String getTipoCuenta() {
        return tipoCuenta;
    }

    // Getter para saldo
    public double getSaldo() {
        return saldo;
    }

    //Verifica si la contraseña es correcta
    public boolean verificarContrasena(String contrasena) {
        return this.contrasena.equals(contrasena);
    }

    //depositar dinero
    public void depositar(double cantidad) {
        if (cantidad > 0) {
            saldo += cantidad;
            System.out.println("Has depositado: $" + cantidad);
        } else {
            System.out.println("Cantidad no válida.");
        }
    }

    //retirar dinero
    public void retirar(double cantidad) {
        if (cantidad > 0 && cantidad <= saldo) {
            saldo -= cantidad;
            System.out.println("Has retirado: $" + cantidad);
        } else {
            System.out.println("Cantidad no válida o saldo insuficiente.");
        }
    }

    //transferir dinero a otro cliente
    public void transferir(Cliente destinatario, double cantidad) {
        if (cantidad > 0 && cantidad <= saldo) {
            saldo -= cantidad;
            destinatario.depositar(cantidad);
            System.out.println("Has transferido $" + cantidad + " a " + destinatario.getNombre());
        } else {
            System.out.println("Transferencia no válida o saldo insuficiente.");
        }
    }
}