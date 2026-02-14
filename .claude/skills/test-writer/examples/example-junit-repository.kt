@DataJpaTest
class UserRepositoryTest {

    @Autowired
    lateinit var userRepository: UserRepository

    @Test
    fun `이메일로 사용자를 찾아야 함`() {
        // given
        val email = "test@example.com"
        val user = User(email = email, name = "Test")
        userRepository.save(user)

        // when
        val found = userRepository.findByEmail(email)

        // then
        assertThat(found).isNotNull
        assertThat(found?.email).isEqualTo(email)
    }

    @Test
    fun `존재하지 않는 이메일로 조회 시 null을 반환해야 함`() {
        // when
        val found = userRepository.findByEmail("nonexistent@example.com")

        // then
        assertThat(found).isNull()
    }
}
