package com.dev.ecommerce.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.dev.ecommerce.model.Imagem;
import com.dev.ecommerce.model.Produto;
import com.dev.ecommerce.repository.ProdutoRepository;

@Controller
public class ProdutoController {

    private static String static_caminho = System.getProperty("java.io.tmpdir");

    @Autowired
    private ProdutoRepository produtoRepository;

    public void setStatic_caminho(String caminhoImagens) {
        this.static_caminho = caminhoImagens;
    }

    @GetMapping("/admin/produtos/cadastro")
    public ModelAndView cadastrar(Produto produto) {
        ModelAndView mv = new ModelAndView("admin/produtos/cadastro");
        mv.addObject("produto", produto);
        return mv;
    }

    @GetMapping("/admin/produtos/listar")
    public ModelAndView listar() {
        ModelAndView mv = new ModelAndView("admin/produtos/listar");
        mv.addObject("listaProdutos", produtoRepository.findAll());
        return mv;
    }

    @GetMapping("/admin/produtos/editar/{id}")
    public ModelAndView editar(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produto = produtoRepository.findById(id);
        // model.addAttribute("cargo", cargo);
        return cadastrar(produto.get());
    }

    @GetMapping("/admin/produtos/mostrarImagem/{imagem}")
    @ResponseBody
    public byte[] retornarImagem(@PathVariable("imagem") String imagem) throws IOException {
        System.out.println(imagem);
        System.out.println(static_caminho + imagem);
        File imagemArquivo = new File(static_caminho + imagem);
        if (imagem != null || imagem.trim().length() > 0) {
            return Files.readAllBytes(imagemArquivo.toPath());
        }
        return null;
    }

    @GetMapping("/admin/produtos/remover/{id}")
    public ModelAndView remover(@PathVariable("id") Long id) {
        Optional<Produto> produto = produtoRepository.findById(id);
        produtoRepository.delete(produto.get());
        return listar();
    }

    @PostMapping("/admin/produtos/salvar")
    public ModelAndView salvar(@Valid Produto produto, BindingResult result,
            @RequestParam("file") MultipartFile arquivo) {
        
        if (result.hasErrors()) {
            return cadastrar(produto);
        }

        produtoRepository.saveAndFlush(produto); //gerar id produto para salvar imagem posteriormente

        try {
            if (!arquivo.isEmpty()) {
                String nome_arquivo = String.valueOf(produto.getId()) + arquivo.getOriginalFilename();
                byte[] bytes = arquivo.getBytes(); 
                Path caminho = Paths.get(static_caminho, nome_arquivo);
                Imagem imagem = new Imagem(produto, nome_arquivo, static_caminho+nome_arquivo);

                Files.createFile(caminho);
                Files.write(caminho, bytes);

                produto.addImagem(imagem);
                produtoRepository.save(produto);
            }else{
                System.out.println("Arquivo vazio");
                return null;
            }
        } catch (IOException e) {
            System.out.println(e);
            return  null;
        }
        return cadastrar(new Produto());
    }

    public String getStatic_caminho() {
        return this.static_caminho;
    }
}
