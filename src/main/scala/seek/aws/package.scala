package seek

import cats.data.Kleisli
import cats.effect.IO
import org.gradle.api.{GradleException, Project}
import pureconfig._

package object aws {

  object syntax extends HasAwsPluginExtension.ToHasAwsPluginExtensionOps

  object instances {
    implicit val projectHasAwsPluginExtension = new HasAwsPluginExtension[Project] {
      def awsExt(p: Project) =
        p.getExtensions.getByType(classOf[AwsPluginExtension])
    }
  }

  def raiseError[A](msg: String): IO[A] =
    IO.raiseError(new GradleException(msg))

  def maybeRun[C](
      shouldRun: LazyProperty[Boolean],
      runnable: Kleisli[IO, C, Boolean],
      onTrue: IO[Unit] = IO.unit,
      onFalse: IO[Unit] = IO.unit): Kleisli[IO, C, Unit] =
    Kleisli { c =>
      shouldRun.run.flatMap {
        case false => IO.unit
        case true  =>
          runnable.run(c).flatMap {
            case false => IO.unit
            case true  => onTrue
          }
      }
    }

  def pascalToCamelCase(s: String): String =
    ConfigFieldMapping(PascalCase, CamelCase).apply(s)

  def camelToKebabCase(s: String): String =
    ConfigFieldMapping(CamelCase, KebabCase).apply(s)

  def camelToSnakeCase(s: String): String =
    ConfigFieldMapping(CamelCase, SnakeCase).apply(s)

  def camelToDotCase(s: String): String =
    ConfigFieldMapping(CamelCase, new StringDelimitedNamingConvention(".")).apply(s)

}
