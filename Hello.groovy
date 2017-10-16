@RestController
class Hello {

  @GetMapping('/hello')
  def String sayhello() {
    return 'Hello!'
  }
}
