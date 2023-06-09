package com.dev.ecommerce.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.dev.ecommerce.model.Endereco;
import com.dev.ecommerce.model.Cliente;
import com.dev.ecommerce.model.Compra;
import com.dev.ecommerce.model.ItensCompra;
import com.dev.ecommerce.model.Produto;
import com.dev.ecommerce.repository.EnderecoRepository;
import com.dev.ecommerce.repository.ClienteRepository;
import com.dev.ecommerce.repository.CompraRepository;
import com.dev.ecommerce.repository.ItensCompraRepository;
import com.dev.ecommerce.repository.ProdutoRepository;

@Controller
public class CarrinhoController {

    private List<ItensCompra> itensCompra = new ArrayList<ItensCompra>();
    private Compra compra = new Compra();
    private Cliente cliente;
    private Endereco endereco;

    @Autowired
    private EnderecoRepository repositoryEndereco;

    @Autowired
    private ProdutoRepository repositoryProduto;

    @Autowired
    private ClienteRepository repositoryCliente;

    @Autowired
    private CompraRepository repositoryCompra;

    @Autowired
    private ItensCompraRepository repositoryItensCompra;

    private void calcularTotal() {
        compra.setValorTotal(0.);
        for (ItensCompra it : itensCompra) {
            compra.setValorTotal(compra.getValorTotal() + it.getValorTotal() );
        }
    }

    @GetMapping("/carrinho")
    public ModelAndView chamarCarrinho() {
        ModelAndView mv = new ModelAndView("cliente/carrinho");
        calcularTotal();
        // System.out.println(compra.getValorTotal());
        mv.addObject("compra", compra);
        mv.addObject("listaItens", itensCompra);
        return mv;
    }

    private void buscarUsuarioLogado() {
        Authentication autenticado = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticado instanceof AnonymousAuthenticationToken)) {
            String email = autenticado.getName();
            cliente = repositoryCliente.buscarClienteEmail(email).get(0);
        }
    }

    @GetMapping("/frete")
    public ModelAndView freteCompra( ) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("cliente/frete");
        calcularTotal();

        Optional<Cliente> clienteOpt = repositoryCliente.findById(cliente.getId());
        cliente = clienteOpt.get();
        List<Endereco> enderecosList = cliente.getEnderecos();
        List<Double> freteList = new ArrayList<>();
        
        endereco = enderecosList.get(0);
        freteList.add(10.0);
        freteList.add(20.0);
        freteList.add(30.0);
        mv.addObject("compra", compra);
        mv.addObject("listaItens", itensCompra);
        mv.addObject("cliente", cliente);
        mv.addObject("enderecos", endereco);
        mv.addObject("freteList", freteList);
        return mv;

    }

    @GetMapping("/pagamento")
    public ModelAndView pagarCompra(String formaPagamento) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("cliente/pagamento");
        calcularTotal();

        Optional<Cliente> clienteOpt = repositoryCliente.findById(cliente.getId());
        cliente = clienteOpt.get();
        List<Endereco> enderecosList = cliente.getEnderecos();

        endereco = enderecosList.get(0);
        compra.setCliente(cliente);
        compra.setFormaPagamento(formaPagamento);

        mv.addObject("compra", compra);
        mv.addObject("listaItens", itensCompra);
        mv.addObject("cliente", cliente);
        mv.addObject("enderecos", endereco);
        return mv;
    }

    @GetMapping("/finalizar")
    public ModelAndView finalizarCompra(String formaPagamento) {
        buscarUsuarioLogado();
        ModelAndView mv = new ModelAndView("cliente/finalizar");

        Optional<Cliente> clienteOpt = repositoryCliente.findById(cliente.getId());
        cliente = clienteOpt.get();
        List<Endereco> enderecosList = cliente.getEnderecos();

        endereco = enderecosList.get(0);

        mv.addObject("compra", compra);
        mv.addObject("listaItens", itensCompra);
        mv.addObject("cliente", cliente);
        mv.addObject("enderecos", endereco);

        compra.setCliente(cliente);
        compra.setFormaPagamento(formaPagamento);
        return mv;
    }

    @PostMapping("/finalizar/confirmar")
    public ModelAndView confirmarCompra(String formaPagamento) {
        ModelAndView mv = new ModelAndView("cliente/compra");
        mv.addObject("compra", compra);
        compra.setCliente(cliente);
        compra.setFormaPagamento(compra.getFormaPagamento());
        repositoryCompra.saveAndFlush(compra);

        for (ItensCompra c : itensCompra) {
            c.setCompra(compra);
            repositoryItensCompra.saveAndFlush(c);
        }
        itensCompra = new ArrayList<>();
        compra = new Compra();
        return mv;
    }

    @GetMapping("/alterarQuantidade/{id}/{acao}")
    public String alterarQuantidade(@PathVariable Long id, @PathVariable Integer acao) {
        ModelAndView mv = new ModelAndView("cliente/carrinho");

        for (ItensCompra it : itensCompra) {
            if (it.getProduto().getId().equals(id)) {
                // System.out.println(it.getValorTotal());
                if (acao.equals(1)) {
                    it.setQuantidade(it.getQuantidade() + 1);
                    it.setValorTotal(0.);
                    it.setValorTotal(it.getValorTotal() + (it.getQuantidade() * it.getValorUnitario()));
                } else if (acao == 0) {
                    it.setQuantidade(it.getQuantidade() - 1);
                    it.setValorTotal(0.);
                    it.setValorTotal(it.getValorTotal() + (it.getQuantidade() * it.getValorUnitario()));
                }
                break;
            }
        }

        return "redirect:/carrinho";
    }

    @GetMapping("/removerProduto/{id}")
    public String removerProdutoCarrinho(@PathVariable Long id) {

        for (ItensCompra it : itensCompra) {
            if (it.getProduto().getId().equals(id)) {
                itensCompra.remove(it);
                break;
            }
        }

        return "redirect:/carrinho";
    }

    @GetMapping("/adicionarCarrinho/{id}")
    public String adicionarCarrinho(@PathVariable Long id) {

        Optional<Produto> prod = repositoryProduto.findById(id);
        Produto produto = prod.get();

        int controle = 0;
        for (ItensCompra it : itensCompra) {
            if (it.getProduto().getId().equals(produto.getId())) {
                it.setQuantidade(it.getQuantidade() + 1);
                it.setValorTotal(0.);
                it.setValorTotal(it.getValorTotal() + (it.getQuantidade() * it.getValorUnitario()));
                controle = 1;
                break;
            }
        }
        if (controle == 0) {
            ItensCompra item = new ItensCompra();
            item.setProduto(produto);
            item.setValorUnitario(produto.getPreco());
            item.setQuantidade(item.getQuantidade() + 1);
            item.setValorTotal(item.getValorTotal() + (item.getQuantidade() * item.getValorUnitario()));

            itensCompra.add(item);

        }
        
        return "redirect:/carrinho";
    }

    @PostMapping("/salvarFrete")
    public ModelAndView salvarFrete(@RequestParam("valorFrete") String valorFrete) {
        double frete = Double.parseDouble(valorFrete);
        
        // Faça o que desejar com o valor do frete, como salvá-lo em uma variável no servidor
        compra.setFrete(frete);
        // Redirecione para a página desejada após salvar o valor do frete
        return new ModelAndView("redirect:/pagamento");
    }

}
