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
import * as React from "react";
import {FormGroup} from "react-bootstrap";
import Button from 'react-bootstrap/Button';
import FormControl from "react-bootstrap/FormControl";
import Modal from 'react-bootstrap/Modal';
import validateUuid from "../../../../validateUuid";

interface IChannelIdProps {
    show: boolean
    handleClose: () => void
    openChannel: (uuid: string) => void
}

interface IChannelIdState {
    isInvalid: boolean
}


class ChannelIdModal extends React.Component<IChannelIdProps, IChannelIdState> {
    private readonly textInput: React.RefObject<any>;

    constructor(readonly props: IChannelIdProps) {
        super(props);

        this.state = {
            isInvalid: false
        };

        this.textInput = React.createRef();
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    public render() {
        return (
            <Modal show={this.props.show} onHide={this.props.handleClose}>
                <Modal.Header>
                    <Modal.Title>Enter Channel Id</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <FormGroup>
                        <FormControl type="text" as="input" placeholder="Channel Id" ref={this.textInput}
                                     isInvalid={this.state.isInvalid}/>
                        <FormControl.Feedback type="invalid">
                            Channel Id has to be a UUID!
                        </FormControl.Feedback>
                    </FormGroup>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={this.props.handleClose}>
                        Cancel
                    </Button>
                    <Button variant="primary" onClick={this.handleSubmit}>
                        Submit
                    </Button>
                </Modal.Footer>
            </Modal>
        );
    }

    private handleSubmit() {
        const value = this.textInput.current!.value;
        const isValid = validateUuid(value);
        this.setState({
            isInvalid: !isValid
        });

        if (isValid) {
            this.props.openChannel(value);
        }
    }

}

export default ChannelIdModal;
