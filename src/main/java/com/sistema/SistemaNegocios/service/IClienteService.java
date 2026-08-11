package com.sistema.SistemaNegocios.service;

import com.sistema.SistemaNegocios.model.Cliente;
import java.util.List;

public interface IClienteService {
    List<Cliente> traerClientes();
    Cliente guardarCliente(Cliente cliente);
    Cliente buscarPorId(Long id);
    void eliminarCliente(Long id);
}