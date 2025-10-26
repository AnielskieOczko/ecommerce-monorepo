package com.rj.ecommerce_backend.testdata
//
//import com.rj.ecommerce.api.shared.core.*
//import com.rj.ecommerce.api.shared.dto.user.common.AuthorityDetails
//import com.rj.ecommerce.api.shared.dto.user.common.UserBaseDetails
//import com.rj.ecommerce.api.shared.dto.user.request.UserCreateRequest
//import com.rj.ecommerce.api.shared.enums.Currency
//import com.rj.ecommerce_backend.product.domain.Category
//import com.rj.ecommerce_backend.product.domain.Product
//import com.rj.ecommerce_backend.product.repository.CategoryRepository
//import com.rj.ecommerce_backend.product.repository.ProductRepository
//import com.rj.ecommerce_backend.product.service.image.ProductImageService
//import com.rj.ecommerce_backend.user.service.AdminService
//import com.rj.ecommerce_backend.user.service.AuthorityService
//import io.github.oshai.kotlinlogging.KotlinLogging
//import jakarta.transaction.Transactional
//import org.springframework.boot.context.event.ApplicationReadyEvent
//import org.springframework.context.annotation.Profile
//import org.springframework.context.event.EventListener
//import org.springframework.core.io.Resource
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver
//import org.springframework.stereotype.Component
//import org.springframework.web.multipart.MultipartFile
//import java.io.ByteArrayInputStream
//import java.io.File
//import java.io.IOException
//import java.io.InputStream
//import java.math.BigDecimal
//import java.nio.file.Files
//import java.nio.file.Path
//import java.time.LocalDate
//
//private val logger = KotlinLogging.logger {}
//
//@Component
//@Profile("dev", "local") // Only run this loader in development environments
//class TestDataLoader(
//    private val adminService: AdminService,
//    private val authorityService: AuthorityService,
//    private val categoryRepository: CategoryRepository,
//    private val productRepository: ProductRepository,
//    private val productImageService: ProductImageService,
//    private val resourcePatternResolver: PathMatchingResourcePatternResolver
//) {
//
//    companion object {
//        private const val ROLE_ADMIN = "ROLE_ADMIN"
//        private const val ROLE_USER = "ROLE_USER"
//    }
//
//    // Use @Transactional on the public method to cover the entire data loading process.
//    @Transactional
//    @EventListener(ApplicationReadyEvent::class)
//    fun onApplicationStartUp() {
//        logger.info { "--- Starting Test Data Loading ---" }
//
//        if (userRepository.count() > 0) {
//            logger.info { "Database already contains data. Skipping test data loader." }
//            return
//        }
//
//        loadAuthorities()
//        loadUsers()
//        createInitialCategories()
//        createInitialProducts(20)
//
//        logger.info { "--- Finished Test Data Loading ---" }
//    }
//
//    private fun loadAuthorities() {
//        logger.info { "Loading test authorities..." }
//        authorityService.createAuthority(AuthorityDetails(1,ROLE_USER))
//        authorityService.createAuthority(AuthorityDetails(2, ROLE_ADMIN))
//    }
//
//    private fun loadUsers() {
//        logger.info { "Loading test users..." }
//
//        // Create Admin User
//        val adminDetails = UserBaseDetails(
//            firstName = "Admin",
//            lastName = "User",
//            address = Address("123 Admin St", "Tech City", ZipCode("12-345"), "Systemland"),
//            phoneNumber = PhoneNumber("555-010-0000"),
//            dateOfBirth = LocalDate.of(1990, 1, 1)
//        )
//        val adminRequest = UserCreateRequest(
//            userDetails = adminDetails,
//            email = "admin@example.com",
//            password = "password",
//            authorities = setOf(ROLE_ADMIN, ROLE_USER)
//        )
//        adminService.createUser(adminRequest)
//
//        // Generate 20 additional regular users
//        for (i in 1..20) {
//            val userDetails = UserBaseDetails(
//                firstName = "FirstName$i",
//                lastName = "LastName$i",
//                address = Address("Street $i", "City $i", ZipCode(String.format("%05d", i)), "Country $i"),
//                phoneNumber = PhoneNumber(String.format("555-555-%04d", i)),
//                dateOfBirth = LocalDate.now().minusYears(20L + i)
//            )
//            val userRequest = UserCreateRequest(
//                userDetails = userDetails,
//                email = "user$i@example.com",
//                password = "password",
//                authorities = setOf(ROLE_USER)
//            )
//
//            try {
//                adminService.createUser(userRequest)
//            } catch (e: Exception) {
//                logger.error(e) { "Error creating user $i: ${e.message}" }
//            }
//        }
//    }
//
//    private fun createInitialCategories() {
//        logger.info { "Loading test categories..." }
//        val categoryNames = listOf(
//            "T-Shirts", "Shirts", "Jeans", "Dresses", "Skirts",
//            "Jackets", "Sweaters", "Hoodies", "Pants", "Shorts"
//        )
//
//        categoryNames.forEach { name ->
//            if (categoryRepository.findByName(name) == null) {
//                categoryRepository.save(Category(name = name))
//                logger.debug { "Created category: $name" }
//            }
//        }
//    }
//
//    private fun createInitialProducts(numProducts: Int) {
//        logger.info { "Loading $numProducts test products..." }
//        val allCategories = categoryRepository.findAll()
//        if (allCategories.isEmpty()) {
//            logger.warn { "No categories found. Cannot create products." }
//            return
//        }
//
//        val imageResources = try {
//            resourcePatternResolver.getResources("classpath:static/product-images/*")
//        } catch (e: IOException) {
//            logger.error(e) { "Error loading image resources. Products will be created without images." }
//            emptyArray<Resource>()
//        }
//
//        if (imageResources.isEmpty()) {
//            logger.warn { "No product images found in classpath:static/product-images/" }
//        }
//
//        (1..numProducts).forEach { i ->
//            try {
//                val product = createProduct(i, allCategories)
//                val savedProduct = productRepository.save(product)
//
//                if (imageResources.isNotEmpty()) {
//                    val imageResource = imageResources[(i - 1) % imageResources.size]
//                    addImageToProduct(savedProduct, imageResource)
//                }
//            } catch (e: Exception) {
//                logger.error(e) { "Error creating product $i: ${e.message}" }
//            }
//        }
//    }
//
//    private fun createProduct(index: Int, categories: List<Category>): Product {
//        val productName = "Product $index"
//        return Product(
//            name = ProductName(productName),
//            description = ProductDescription("Description for $productName"),
//            unitPrice = Money(BigDecimal.valueOf(10.00 + index), Currency.USD),
//            quantityInStock = QuantityInStock(10 + index)
//        ).apply {
//            this.categories.add(categories.random()) // .random() is an idiomatic way to get a random element
//        }
//    }
//
//    private fun addImageToProduct(product: Product, imageResource: Resource) {
//        try {
//            val imagePath = Path.of(imageResource.uri)
//            val fileName = imagePath.fileName.toString()
//            val fileContent = Files.readAllBytes(imagePath)
//            val contentType = Files.probeContentType(imagePath) ?: "application/octet-stream"
//
//            val multipartFile = CustomMultipartFile(fileContent, fileName, contentType)
//
//            // Assuming ProductImageService handles adding the image to the product and saving.
//            productImageService.addImagesToProduct(product.id!!, listOf(multipartFile), "Product Image")
//        } catch (e: IOException) {
//            logger.error(e) { "Error processing image for product ${product.id}: ${e.message}" }
//        }
//    }
//
//    // A private inner class is a perfect fit here.
//    private class CustomMultipartFile(
//        private val content: ByteArray,
//        private val name: String,
//        private val contentType: String
//    ) : MultipartFile {
//        override fun getName(): String = name
//        override fun getOriginalFilename(): String = name
//        override fun getContentType(): String = contentType
//        override fun isEmpty(): Boolean = content.isEmpty()
//        override fun getSize(): Long = content.size.toLong()
//        override fun getBytes(): ByteArray = content
//        override fun getInputStream(): InputStream = ByteArrayInputStream(content)
//        override fun transferTo(dest: File) = Files.write(dest.toPath(), content)
//    }
//}