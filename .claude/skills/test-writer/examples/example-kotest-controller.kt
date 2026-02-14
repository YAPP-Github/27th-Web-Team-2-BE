@WebMvcTest(UserController::class)
class UserControllerTest : FunSpec({

    lateinit var mockMvc: MockMvc
    lateinit var userService: UserService

    beforeTest {
        mockMvc = testContextManager().testContext.getBean(MockMvc::class.java)
        userService = mockk()
    }

    context("GET /users/{id}") {
        test("사용자가 존재할 때 200 OK와 사용자 정보를 반환해야 함") {
            // given
            val userId = 1L
            val user = User(id = userId, name = "John", email = "john@example.com")
            every { userService.getUser(userId) } returns user

            // when & then
            mockMvc.get("/users/$userId")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.id") { value(userId) }
                    jsonPath("$.name") { value("John") }
                    jsonPath("$.email") { value("john@example.com") }
                }
        }

        test("사용자가 존재하지 않을 때 404 Not Found를 반환해야 함") {
            // given
            val userId = 999L
            every { userService.getUser(userId) } throws UserNotFoundException()

            // when & then
            mockMvc.get("/users/$userId")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    context("POST /users") {
        test("유효한 요청으로 사용자를 생성하고 201 Created를 반환해야 함") {
            // given
            val request = CreateUserRequest(email = "new@example.com", name = "New User")
            val createdUser = User(id = 1L, email = request.email, name = request.name)
            every { userService.createUser(any(), any()) } returns createdUser

            // when & then
            mockMvc.post("/users") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isCreated() }
                jsonPath("$.id") { value(1L) }
                jsonPath("$.email") { value("new@example.com") }
            }
        }
    }
})
