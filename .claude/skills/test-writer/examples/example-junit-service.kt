@SpringBootTest
class UserServiceTest {

    @Autowired
    lateinit var userService: UserService

    @MockBean
    lateinit var userRepository: UserRepository

    @Test
    fun `사용자가 존재할 때 사용자를 반환해야 함`() {
        // given
        val userId = 1L
        val user = User(id = userId, name = "John")
        given(userRepository.findById(userId)).willReturn(Optional.of(user))

        // when
        val result = userService.getUser(userId)

        // then
        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `사용자를 찾을 수 없을 때 예외를 발생시켜야 함`() {
        // given
        val userId = 999L
        given(userRepository.findById(userId)).willReturn(Optional.empty())

        // when & then
        assertThrows<UserNotFoundException> {
            userService.getUser(userId)
        }
    }
}
