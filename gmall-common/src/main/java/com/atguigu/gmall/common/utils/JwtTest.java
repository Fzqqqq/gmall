package com.atguigu.gmall.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
	private static final String pubKeyPath = "D:\\project\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\project\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE2MDI4NTQwMTF9.WiPItQ4N2NN4ZvXJRzUhWyyhGSIjMI0v-G9Tv_TGe_MKBBMQnif5V4YU4hAoSAM_cNuNMKZQhZl71O8JeBZcNHvH7Iufm9MoFWhCyH_6vRepjZDjndAIGLC8B-l2rwZdSX_R9FjxYD9kTr8kvy6x7sXKUS6_JW_7Swo2ykJpmGTOkEZSJzWKJZTYcWczh1Okl_VcHbi53R3vAhzQL9gpZVxUf-_U58aSjX9bbdGLNvkajFY4-7SaVYdUeNbd62WsUjUVCT2fXE3WEFr75a7CyZY4Y1cYhW1cw1l8Ei8ogKUPYlZiGmLSmo7OOfNRwQkkiFsyBd7clRocT4jd2p-qCg";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}