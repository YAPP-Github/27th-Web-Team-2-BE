@SpringBootTest
class UserServiceTest : FunSpec({

    lateinit var userService: UserService
    lateinit var userRepository: UserRepository

    beforeTest {
        userService = testContextManager().testContext.getBean(UserService::class.java)
        userRepository = mockk()
    }

    context("사용자 조회") {
        test("사용자가 존재할 때 사용자를 반환해야 함") {
            // given
            val userId = 1L
            val user = User(id = userId, name = "John")
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = userService.getUser(userId)

            // then
            result shouldBe user
        }

        test("사용자를 찾을 수 없을 때 예외를 발생시켜야 함") {
            // given
            val userId = 999L
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            shouldThrow<UserNotFoundException> {
                userService.getUser(userId)
            }
        }
    }

    context("사용자 생성") {
        test("유효한 정보로 사용자를 생성해야 함") {
            // given
            val email = "test@example.com"
            val name = "John"
            val user = User(email = email, name = name)
            every { userRepository.save(any()) } returns user

            // when
            val result = userService.createUser(email, name)

            // then
            result.email shouldBe email
            result.name shouldBe name
            verify(exactly = 1) { userRepository.save(any()) }
        }
    }
})
