package com.sistema.SistemaNegocios.service;

import com.sistema.SistemaNegocios.model.Cliente;
import com.sistema.SistemaNegocios.repository.IClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService implements IClienteService {

    private final IClienteRepository clienteRepository;

    public ClienteService(IClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> traerClientes() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente guardarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente buscarPorId(Long id) {
        return clienteRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminarCliente(Long id) {
        clienteRepository.deleteById(id);
    }
}