package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService;

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public void excluir(String id) {
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            clienteRepository.deleteById(id);

            // Audita a exclusão do cliente
            auditoriaService.registrarAuditoria("EXCLUIR", "Cliente", id, "Todos",
                    cliente.getNome(), null, "Exclusão de cliente do sistema");
        }
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    public Cliente salvar(Cliente cliente) {
        // Se for novo cliente, salva e audita a INSERÇÃO
        if (cliente.getId() == null || cliente.getId().isEmpty()) {
            Cliente salvo = clienteRepository.save(cliente);

            auditoriaService.registrarAuditoria("INSERIR", "Cliente", salvo.getId(), "Todos",
                    null, salvo.getNome(), "Cadastro de novo cliente");

            return salvo;
        } else {
            // Busca o cliente antigo no banco para comparar as mudanças
            Optional<Cliente> existenteOpt = clienteRepository.findById(cliente.getId());

            if (existenteOpt.isPresent()) {
                Cliente existente = existenteOpt.get();

                // Verifica e audita se o nome mudou
                if (existente.getNome() != null && !existente.getNome().equals(cliente.getNome())) {
                    auditoriaService.registrarAuditoria("ATUALIZAR", "Cliente", cliente.getId(), "nome",
                            existente.getNome(), cliente.getNome(), "Alteração de nome do cliente");
                }

                // Verifica e audita se o telefone mudou
                if (existente.getTelefone() != null && !existente.getTelefone().equals(cliente.getTelefone())) {
                    auditoriaService.registrarAuditoria("ATUALIZAR", "Cliente", cliente.getId(), "telefone",
                            existente.getTelefone(), cliente.getTelefone(), "Alteração de telefone de contato");
                }

                // Verifica e audita se o logradouro mudou
                if (existente.getLogradouro() != null && !existente.getLogradouro().equals(cliente.getLogradouro())) {
                    auditoriaService.registrarAuditoria("ATUALIZAR", "Cliente", cliente.getId(), "logradouro",
                            existente.getLogradouro(), cliente.getLogradouro(), "Alteração de endereço");
                }

                // Salva a atualização com os novos dados
                return clienteRepository.save(cliente);
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }
}