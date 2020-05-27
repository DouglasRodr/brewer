package com.algaworks.brewer.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.algaworks.brewer.model.Cliente;
import com.algaworks.brewer.repository.Clientes;
import com.algaworks.brewer.service.exception.CpfCnpjClienteJaCadastradoException;
import com.algaworks.brewer.service.exception.ImpossivelExcluirEntidadeException;

@Service
public class CadastroClienteService {

	@Autowired
	private Clientes clientes;
	
	@Transactional
	public void salvar(Cliente cliente) {
		Optional<Cliente> clienteExistente = clientes.findByCpfOuCnpj(cliente.getCpfOuCnpjSemFormatacao());
		
		if ((cliente.isNovo() && clienteExistente.isPresent())
			|| (clienteExistente.isPresent() && !clienteExistente.get().equals(cliente))){
			throw new CpfCnpjClienteJaCadastradoException("CPF/CNPJ já cadastrado");
		}
		
		clientes.save(cliente);
	}

	public Cliente buscar(Long codigo) {
		Cliente cliente = clientes.buscar(codigo);
		cliente.getEndereco().setEstado(cliente.getEndereco().getCidade().getEstado());
		return cliente;
	}
	
	@Transactional
	public void excluir(Cliente cliente) {
		try {
			this.clientes.delete(cliente);
			this.clientes.flush();
		} catch (DataIntegrityViolationException e) {
			throw new ImpossivelExcluirEntidadeException("Impossível apagar cliente. Já está atrelado a alguma venda.");
		}
	}
	
}