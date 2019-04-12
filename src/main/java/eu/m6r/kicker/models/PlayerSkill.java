/*
 * This file is part of kicker (https://github.com/mbrtargeting/kicker).
 * Copyright (c) 2019 Jan Graßegger.
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

package eu.m6r.kicker.models;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PlayerSkill implements Serializable {

    public PlayerSkill() {
    }

    @Id
    public String id;
    public String name;
    public String avatarImage;
    public int games;
    public int wins;
    public double skill;
}
