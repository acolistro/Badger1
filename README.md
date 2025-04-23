# Badger

Badger is a secure task and list management app that puts privacy first. With end-to-end encryption, secure notifications, and seamless list sharing, Badger helps you stay organized while keeping your data private.

![Badger App](app/src/main/res/drawable/badger_cute.png)

## 🚀 Features

### 📝 Task & List Management
- Create, edit, and organize shareable lists
- Easy task prioritization and completion tracking
- Intuitive user interface for efficient task management
- Support for list favorites for quick access

### 🔒 End-to-End Encryption (E2EE)
- All list content is encrypted on your device before transmission
- Unique encryption keys for each list ensure compartmentalized security
- Even if servers are compromised, your data remains protected
- Secure key management for seamless sharing without compromising security

### 🔔 Secure Notifications
- Notifications protected using Signal protocols
- Receive real-time updates without exposing list content
- Know when lists are modified and by whom without revealing sensitive information
- Privacy-preserving notification system

### 👥 Secure Sharing
- Share lists with other users without compromising encryption
- Granular access control for shared lists
- Secure key exchange ensures only authorized users can decrypt shared content
- Easy revocation of access when needed

## 🛡️ Security Architecture

Badger implements a comprehensive security model to protect your data:

### End-to-End Encryption
- **AES-256 GCM** encryption for list content
- **RSA-2048** asymmetric encryption for secure key exchange
- **Android Keystore** integration for hardware-backed security when available
- Encrypted storage for both local and cloud data

### Key Management
- Each list has its own unique encryption key
- Keys are never transmitted in plaintext
- When sharing lists, keys are securely encrypted for each recipient
- Key rotation support for enhanced security

### Access Control
- Cryptographic access control ensures data security
- Users can only decrypt lists they have explicit access to
- Access revocation removes encryption keys from unauthorized users
- Secure deletion of revoked keys

## 🧪 Testing & Quality Assurance

Badger implements extensive testing practices:

### Unit Testing
- Comprehensive test coverage for encryption components
- Mocked cryptography services for deterministic testing
- Repository testing ensures correct encryption/decryption flows
- ViewModels tested with secured data flows

### Integration Testing
- End-to-end tests for the complete encryption workflow
- Key sharing and rotation testing
- Error case handling and recovery testing
- Notification security verification

### Security Auditing
- Regular security reviews of encryption implementation
- Test cases for potential security vulnerabilities
- Verification of encryption key handling
- Validation of secure sharing mechanisms

## 📱 Platform Support

- Currently available for Android
- Designed with cross-platform capability in mind
- Future iOS support planned

## ⚙️ Technical Details

Badger is built using modern Android development practices:

- **Kotlin** as primary language
- **MVVM architecture** with clean separation of concerns
- **Room** for local database storage
- **Firebase** for authentication and secure synchronization
- **Coroutines & Flow** for asynchronous operations
- **Hilt** for dependency injection
- **AndroidX** components for UI and lifecycle management

## 🚧 Development Status

Badger is currently in **Early Development** stage:
- Core architecture is in place
- Security framework implementation in progress
- Active development of features ongoing
- UI/UX design and implementation underway

## 🔜 Roadmap

- Complete E2EE implementation and testing
- Implement secure notification system
- Enhance list sharing capabilities
- User interface refinements
- Alpha and beta testing phases
- iOS version development
- Public release

## 📋 Requirements

- Android 8.0 (API level 26) or higher
- Google Play Services

## 💾 Installation

As the app is still in early development, it's not yet available for general installation. A test version will be distributed to selected users during the alpha stage.

## 🤝 Contributing

Badger is not currently open source, but we plan to open the codebase in the future. If you're interested in contributing or testing, please contact the development team.

## 📄 License

All rights reserved. Future open-source release license to be determined.

---

*Badger: Secure task management for a privacy-conscious world.*