/*
 * This file is part of MrFoosball (https://github.com/gesundkrank/mrfoosball).
 * Copyright (c) 2020 Jan Graßegger.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 Declaration files are how the Typescript compiler knows about the type information(or shape) of an object.
 They're what make intellisense work and make Typescript know all about your code.

 A wildcard module is declared below to allow third party libraries to be used in an app even if they don't
 provide their own type declarations.

 To learn more about using third party libraries in an Ionic app, check out the docs here:
 http://ionicframework.com/docs/v2/resources/third-party-libs/

 For more info on type definition files, check out the Typescript docs here:
 https://www.typescriptlang.org/docs/handbook/declaration-files/introduction.html
 */
declare module '*';
