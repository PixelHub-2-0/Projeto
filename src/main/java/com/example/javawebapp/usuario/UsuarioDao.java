package com.example.javawebapp.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.sql.Statement;

import org.eclipse.tags.shaded.org.apache.xpath.operations.Bool;
import com.example.javawebapp.db.Conexao;
import at.favre.lib.crypto.bcrypt.BCrypt;



public class UsuarioDao {
    //cadastrar
    //listar
    private static Integer idAtual = 0;
    
    //INSERT INTO Usuarios VALUES(?)
    public static Usuario cadastrar(String nome, String email, String senha){
        Usuario usuario = null;
        String hashSenha = BCrypt.withDefaults().hashToString(12, senha.toCharArray());        
        String sql = "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?);";
        try (
            Connection connection = Conexao.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);//??
        ) {
            statement.setString(1, nome);
            statement.setString(2, email);
            statement.setString(3, hashSenha);
            statement.executeUpdate();

            ResultSet rs = statement.getGeneratedKeys();

            if(rs.next()) {
                usuario = new Usuario(rs.getInt(1), nome, email, hashSenha);
            }

            rs.close();

            return usuario;  
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Usuario> listarTodos() {
        String sql = "SELECT * FROM usuarios;";
        List<Usuario> usuarios = new ArrayList<>();

        try (
            Connection connection = Conexao.getConnection();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql);
        ) {
            while(rs.next()) {
                usuarios.add(
                    new Usuario(
                        rs.getInt("id"), 
                        rs.getString("nome"), 
                        rs.getString("email"), 
                        rs.getString("senha")
                    )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            return usuarios;
        } 

        return usuarios;
        
    }


    //SELECT * FROM Usuarios WHERE id = ?
    //Entrada = id
    //Saida = usuario
    public static Usuario buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ?;";

        try (
            Connection connection = Conexao.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, email);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return new Usuario(
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("senha")
                );
            }

            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    //Login
    //Entrada = Email e
    //Saida = Usuario/null ou Boolean
    public static Boolean login(String email, String senha) {
        Usuario usuario = buscarPorEmail(email);
        if (usuario != null) {
            BCrypt.Result result = BCrypt.verifyer().verify(senha.toCharArray(), usuario.getSenha());
            return result.verified;
        }
        return false;
    }

    //Entrada = email
    //Saida = boolean
    
    public static Boolean existeComEmail(String email) {
        return buscarPorEmail(email) != null;
    }
}

