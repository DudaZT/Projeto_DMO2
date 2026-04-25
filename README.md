# 📱 Micro Rede Social - Android

Projeto desenvolvido para a disciplina **Dispositivos Móveis 2 (ARQDMO2)** do curso de **Análise e Desenvolvimento de Sistemas - IFSP Araraquara**.

---

## 🎯 Sobre o Projeto

Este aplicativo consiste em uma **micro rede social**, onde usuários podem:

* Criar conta e fazer login
* Publicar fotos com descrição
* Compartilhar sua localização (cidade)
* Visualizar postagens de outros usuários
* Buscar posts por cidade
* Editar perfil

O objetivo do projeto é aplicar conceitos de desenvolvimento Android com integração a serviços remotos e uso de localização.

---

## 🚀 Funcionalidades

### 🔐 Autenticação e Cadastro

* Login com e-mail e senha
* Cadastro de novos usuários
* Autenticação com **Firebase Authentication**
* Redirecionamento automático caso o usuário já esteja logado

---

### 📸 Postagens

* Upload de imagem pela galeria
* Inserção de descrição
* Captura automática da localização (cidade)
* Armazenamento no **Firebase Firestore**

---

### 📰 Feed e Interações

* Feed com paginação (5 posts por vez)
* Scroll infinito
* Busca de postagens por cidade
* Exibição de autor, descrição, imagem e localização

---

### 👤 Perfil

* Edição de nome e username
* Alteração de senha
* Atualização de foto de perfil

---

### 📍 Localização

* Captura da localização via GPS
* Conversão de coordenadas em cidade (Geocoder)

---

## 🛠️ Tecnologias Utilizadas

* **Kotlin**
* **Android Studio (API 33 - Android 13)**
* **Firebase Authentication**
* **Firebase Firestore**
* **RecyclerView**
* **Fused Location Provider (GPS)**
* **Geocoder**
* **ViewBinding**

---

## 📱 Responsividade

O aplicativo foi desenvolvido utilizando boas práticas de UI/UX:

* Uso de `match_parent` e `wrap_content`
* Unidades `dp` e `sp`
* `ScrollView` para evitar cortes de conteúdo
* Testes em diferentes tamanhos de tela e orientação

---

## 📂 Estrutura do Projeto

```
📦 app
 ┣ 📂 ui (Activities)
 ┣ 📂 adapter (RecyclerView)
 ┣ 📂 data (Modelos)
 ┣ 📂 util (Helpers e conversores)
```

---

## 🔥 Integração com Firebase

* **Authentication:** gerenciamento de login e cadastro
* **Firestore:** armazenamento de usuários e postagens

---

## ▶️ Demonstração

📌 Vídeo curto (30s):
👉 [Download](video/videodemonstracao.mp4)

📌 Vídeo explicativo (5–10 min):
👉 [Download](video/videoexplicativo.mp4)

---

## 📚 Aprendizados

Durante o desenvolvimento foram aplicados conceitos como:

* Integração com APIs (Firebase)
* Manipulação de imagens (Base64)
* Paginação com Firestore
* Permissões no Android (GPS)
* Organização de código em camadas

---

## 👨‍🏫 Informações Acadêmicas

* **Disciplina:** Dispositivos Móveis 2
* **Instituição:** IFSP - Campus Araraquara

---

## 👩‍💻 Desenvolvido por

* Maria Eduarda Zanetti Moro

---

## 📌 Observações

Este projeto tem fins acadêmicos e foi desenvolvido como parte de uma avaliação.
