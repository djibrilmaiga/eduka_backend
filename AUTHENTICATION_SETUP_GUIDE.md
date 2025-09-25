# Guide de Configuration - Système d'Authentification JWT Eduka

## Vue d'ensemble

Ce guide vous accompagne dans la configuration du système d'authentification JWT complet pour l'application Eduka. Le système supporte :

- **Authentification standard** pour les utilisateurs actifs (Parrain, Organisation, Admin)
- **Authentification OTP** pour les tuteurs (acteurs passifs)
- **Gestion des mots de passe** (oubli, réinitialisation, changement)
- **Tokens JWT** avec access et refresh tokens
- **Sécurité renforcée** avec variables d'environnement

## 🔧 Configuration Requise

### 1. Variables d'Environnement

Créez un fichier `.env` à la racine du projet avec les variables suivantes :

\`\`\`bash
# Base de données
DB_URL=jdbc:mysql://localhost:3306/eduka_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT Configuration (OBLIGATOIRE - Générez des clés sécurisées)
JWT_SECRET=your-very-long-secret-key-here-minimum-256-bits-must-be-changed-in-production
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Configuration Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# Configuration Redis (pour OTP)
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Configuration OTP
OTP_EXPIRATION_MINUTES=5
OTP_MAX_ATTEMPTS=3

# URL Frontend (pour les liens de réinitialisation)
FRONTEND_URL=http://localhost:3000
\`\`\`

### 2. Génération de Clé JWT Sécurisée

**IMPORTANT** : Générez une clé secrète forte pour la production :

\`\`\`bash
# Méthode 1 : OpenSSL
openssl rand -base64 64

# Méthode 2 : Java
java -cp . -c "System.out.println(java.util.Base64.getEncoder().encodeToString(new java.security.SecureRandom().generateSeed(64)));"

# Méthode 3 : En ligne (pour développement uniquement)
# https://generate-random.org/api-key-generator?count=1&length=64&type=mixed-numbers-symbols
\`\`\`

### 3. Configuration Base de Données

Assurez-vous que MySQL est installé et configuré :

\`\`\`sql
-- Créer la base de données
CREATE DATABASE eduka_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Créer un utilisateur dédié (recommandé)
CREATE USER 'eduka_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON eduka_db.* TO 'eduka_user'@'localhost';
FLUSH PRIVILEGES;
\`\`\`

### 4. Configuration Redis (pour OTP)

Installez et démarrez Redis :

\`\`\`bash
# Ubuntu/Debian
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server

# macOS avec Homebrew
brew install redis
brew services start redis

# Docker
docker run -d -p 6379:6379 --name redis redis:alpine
\`\`\`

### 5. Configuration Email

Pour Gmail, activez l'authentification à 2 facteurs et générez un mot de passe d'application :

1. Allez dans votre compte Google
2. Sécurité → Authentification à 2 facteurs
3. Mots de passe d'application → Générer
4. Utilisez ce mot de passe dans `MAIL_PASSWORD`

## 🚀 Démarrage

### 1. Installation des Dépendances

\`\`\`bash
mvn clean install
\`\`\`

### 2. Démarrage de l'Application

\`\`\`bash
mvn spring-boot:run
\`\`\`

### 3. Vérification

L'application démarre sur `http://localhost:8080`

- Documentation API : `http://localhost:8080/swagger-ui.html`
- Santé de l'app : `http://localhost:8080/actuator/health`

## 📚 Utilisation des APIs

### Authentification Utilisateurs Standard

#### Connexion
\`\`\`bash
POST /api/auth/login
Content-Type: application/json

{
