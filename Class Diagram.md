---
title: PizzaStore Class Diagram
---

classDiagram
direction TB

      %% ── Models ──
      class CrustType {
          -Long crustId
          -String crustName
          -BigDecimal price
          +getCrustId() Long
          +setCrustId(Long) void
          +getCrustName() String
          +setCrustName(String) void
          +getPrice() BigDecimal
          +setPrice(BigDecimal) void
      }

      class MenuItem {
          -Long id
          -String title
          -String description
          -BigDecimal price
          -String imageUrl
          +getters/setters()
      }

      class Topping {
          -Long id
          -String name
          -BigDecimal price
          +getters/setters()
      }

      class Promotion {
          -Long id
          -String code
          -String description
          -BigDecimal discountAmount
          +getters/setters()
      }

      class User {
          <<Entity>>
          -Long id
          -String email
          -String firstName
          -String lastName
          -String phoneNumber
          -String address
          -String password
          -String userType
          +getters/setters via Lombok()
      }

      class Order {
          -Long id
          -Long userId
          -BigDecimal totalAmount
          -String status
          -String deliveryMethod
          -String address
          -LocalDateTime orderDate
          +getters/setters()
      }

      %% ── Repositories ──
      class CrustTypeRepository {
          <<Repository>>
          -DSLContext dsl
          +findAll() List~CrustType~
      }

      class MenuItemRepository {
          <<Repository>>
          -DSLContext dsl
          +findAll() List~MenuItem~
          +save(MenuItem) void
          +deleteById(Long) void
      }

      class ToppingRepository {
          <<Repository>>
          -DSLContext dsl
          +findAll() List~Topping~
      }

      class PromotionRepository {
          <<Repository>>
          -DSLContext dsl
          +findAll() List~Promotion~
          +save(Promotion) void
          +deleteById(Long) void
      }

      class UserRepository {
          <<Repository>>
          -DSLContext dsl
          -Logger logger
          +findByUsername(String) List~User~
          +save(User) void
      }

      class OrderRepository {
          <<Repository>>
          -DSLContext dsl
          +save(Order) void
          +findById(Long) Order
          +findByUserId(Long) List~Order~
          +findSalesSummary(Date, Date) List~Map~
      }

      %% ── Controllers ──
      class PizzaController {
          <<RestController>>
          -CrustTypeRepository crustTypeRepository
          -ToppingRepository toppingRepository
          -MenuItemRepository menuItemRepository
          +getCrustTypes() List~CrustType~
          +getToppings() List~Topping~
          +getMenuItems() List~MenuItem~
      }

      class MenuManagementController {
          <<RestController>>
          -MenuItemRepository menuItemRepository
          +addItem(MenuItem) ResponseEntity
          +updateItem(MenuItem) ResponseEntity
          +deleteItem(Long) ResponseEntity
      }

      class PromotionController {
          <<RestController>>
          -PromotionRepository promotionRepository
          +getPromotions() List~Promotion~
          +addPromotion(Promotion) ResponseEntity
          +deletePromotion(Long) ResponseEntity
      }

      class AuthController {
          <<RestController>>
          -UserRepository userRepository
          -Logger logger
          +getStatus() Map
          +handleSignIn(String, String) ResponseEntity
          +handleSignUp(User) ResponseEntity
      }

      class RestaurantInfoController {
          <<RestController>>
          -PromotionRepository promotionRepository
          +getRestaurantInfo() Map
      }

      class CartController {
          <<RestController>>
          +getCart() Map
          +addToCart(CartItem) ResponseEntity
          +updateQuantity(Long, int) ResponseEntity
          +applyPromotion(String) ResponseEntity
      }

      class CheckoutController {
          <<RestController>>
          -OrderRepository orderRepository
          +getSummary() Map
          +processCheckout(OrderDetails) ResponseEntity
      }

      class ReportingController {
          <<RestController>>
          -OrderRepository orderRepository
          +getSalesReport(Date, Date) List~Map~
      }

      class ProfileController {
          <<RestController>>
          -UserRepository userRepository
          +getProfile() Map
          +updateProfile(User) ResponseEntity
      }

      class OrderController {
          <<RestController>>
          -OrderRepository orderRepository
          +getOrderStatus(Long) Map
          +getUserOrders() List~Order~
      }

      %% ── Angular Components ──
      class Menu {
          <<Component>>
          +Signal~MenuItem[]~ menuItems
          +Signal~Topping[]~ toppings
          +ngOnInit() void
          +onAddToCart(item) void
      }

      class Login {
          <<Component>>
          +Signal~AuthStatus~ authStatus
          +FormGroup loginForm
          +onSignin() void
      }

      class NewAccount {
          <<Component>>
          +FormGroup signupForm
          +onSignup() void
      }

      class Profile {
          <<Component>>
          +Signal~ProfileData~ profile
          +onUpdateProfile() void
      }

      class Cart {
          <<Component>>
          +Signal~CartData~ cart
          +onIncreaseQty(id) void
          +onApplyPromo(code) void
      }

      class Checkout {
          <<Component>>
          +onProcessPayment() void
      }

      class Reporting {
          <<Component>>
          +Signal~Report[]~ reports
          +dateRange FormGroup
          +onGenerateReport() void
      }

      class Admin {
          <<Component>>
          +onAddMenuItem() void
          +onAddPromotion() void
      }

      %% ── Backend Relationships ──
      PizzaController --> CrustTypeRepository : uses
      PizzaController --> ToppingRepository : uses
      PizzaController --> MenuItemRepository : uses
      MenuManagementController --> MenuItemRepository : uses
      PromotionController --> PromotionRepository : uses
      AuthController --> UserRepository : uses
      RestaurantInfoController --> PromotionRepository : uses
      CheckoutController --> OrderRepository : uses
      ReportingController --> OrderRepository : uses
      OrderController --> OrderRepository : uses
      ProfileController --> UserRepository : uses

      CrustTypeRepository --> CrustType : returns
      MenuItemRepository --> MenuItem : returns
      ToppingRepository --> Topping : returns
      PromotionRepository --> Promotion : returns
      UserRepository --> User : returns
      OrderRepository --> Order : returns

      %% ── Frontend-to-Backend API Calls ──
      Menu ..> PizzaController : GET /api/menu-items\nGET /api/crust-types\nGET /api/toppings
      Login ..> AuthController : POST /api/auth/signin
      NewAccount ..> AuthController : POST /api/auth/signup
      Profile ..> ProfileController : GET /api/profile\nPUT /api/profile
      RestaurantInfo ..> RestaurantInfoController : GET /api/restaurant-info
      Cart ..> CartController : GET /api/cart\nPOST /api/cart/add\nPOST /api/cart/promo
      Checkout ..> CheckoutController : POST /api/checkout/process
      Reporting ..> ReportingController : GET /api/reports/sales
      Admin ..> MenuManagementController : POST/PUT/DELETE /api/admin/menu
      Admin ..> PromotionController : POST/DELETE /api/admin/promotions