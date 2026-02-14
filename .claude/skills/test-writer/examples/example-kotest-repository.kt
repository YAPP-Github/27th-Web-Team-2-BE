@DataJpaTest
class UserRepositoryTest : FunSpec({

    lateinit var userRepository: UserRepository

    beforeTest {
        userRepository = testContextManager().testContext.getBean(UserRepository::class.java)
    }

    context("이메일로 사용자 조회") {
        test("사용자가 존재할 때 사용자를 반환해야 함") {
            // given
            val email = "test@example.com"
            val user = User(email = email, name = "Test")
            userRepository.save(user)

            // when
            val found = userRepository.findByEmail(email)

            // then
            found.shouldNotBeNull()
            found.email shouldBe email
        }

        test("존재하지 않는 이메일로 조회 시 null을 반환해야 함") {
            // when
            val found = userRepository.findByEmail("nonexistent@example.com")

            // then
            found.shouldBeNull()
        }
    }

    context("사용자 저장") {
        test("새로운 사용자를 저장할 수 있어야 함") {
            // given
            val user = User(email = "new@example.com", name = "New User")

            // when
            val saved = userRepository.save(user)

            // then
            saved.id.shouldNotBeNull()
            saved.email shouldBe "new@example.com"
        }
    }
})
